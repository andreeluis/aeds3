package db.index.bplustree;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import db.Database;
import model.Register;
import model.interfaces.IndexStrategy;

public class BPlusTree<T extends Register> implements IndexStrategy<T> {
	private int order;
	private String filePath;
	private RandomAccessFile file;

	// Variáveis globais para controle de crescimento da árvore
	private BPlusRegister auxRegister;
	private long auxPagePosition;
	private boolean pageGrew;
	private boolean pageShrunk;

	// order
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		if (order >= 3) {
			this.order = order;
		} else {
			System.out.println("A ordem precisa ser no minimo 3!");
			this.order = 3;
		}
	}

	// filePathName
	public String getFilePath() {
		return this.filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath + "BPlusTreeIndex" + Database.getExtension();
	}

	// file
	public RandomAccessFile getFile() {
		return this.file;
	}

	public void setFile(RandomAccessFile file) throws IOException {
		this.file = file;

		if (this.file.length() < 16) {
			file.writeLong(-1);
			file.writeLong(-1);
		}

		this.file.seek(0);
	}

	private void setFile() throws IOException {
		this.setFile(new RandomAccessFile(this.filePath, "rw"));
	}

	// root
	public long getRoot() throws IOException {
		this.file.seek(0);
		return this.file.readLong();
	}

	public void setRoot(long position) throws IOException {
		this.file.seek(0);
		this.file.writeLong(position);
	}

	// constructor
	public BPlusTree(int order, String filePath) throws Exception {
		this.setOrder(order);
		this.setFilePath(filePath);

		this.setFile();
	}

	@Override
	public long get(int id) throws IOException {
		BPlusRegister register = new BPlusRegister(id, 0);
		long root = this.getRoot();

		// early return
		if (root == -1) {
			return -1;
		}

		ArrayList<BPlusRegister> registers = read(register, root);
		if (registers.isEmpty()) {
			return -1;
		}

		return registers.get(0).getPosition();
	}

	private ArrayList<BPlusRegister> read(BPlusRegister elem, long position) throws IOException {
		// early return
		if (position == -1) {
			return new ArrayList<>();
		}

		BPlusPage page = readPage(position);

		// Encontra o ponto em que a chave deve estar na página
		// Nesse primeiro passo, todas as chaves menores que a chave buscada
		// são ultrapassadas
		int i = 0;
		while (elem != null && i < page.getRegistersSize() && elem.compareTo(page.registers.get(i)) > 0) {
			i++;
		}

		// Chave encontrada (ou pelo menos o ponto onde ela deveria estar).
		// Segundo passo - testa se a chave é a chave buscada e se está em uma folha
		// Obs.: em uma árvore B+, todas as chaves válidas estão nas folhas
		if (i < page.getRegistersSize() && page.isLeaf() && (elem == null || elem.compareTo(page.registers.get(i)) == 0)) {

			// Cria a lista de retorno e insere os elementos encontrados
			ArrayList<BPlusRegister> lista = new ArrayList<>();
			while (elem == null || elem.compareTo(page.registers.get(i)) <= 0) {

				if (elem == null || elem.compareTo(page.registers.get(i)) == 0) {
					lista.add(page.registers.get(i));
				}

				i++;

				// Se chegar ao fim da folha, então avança para a folha seguinte
				if (i == page.getRegistersSize()) {
					if (page.getNext() == -1) {
						break;
					}

					page = readPage(page.getNext());

					i = 0;
				}
			}

			return lista;
		}

		// Terceiro passo - se a chave não tiver sido encontrada nesta folha,
		// testa se ela está na próxima folha. Isso pode ocorrer devido ao
		// processo de ordenação.
		else if (i == page.getRegistersSize() && page.isLeaf()) {

			// Testa se há uma próxima folha. Nesse caso, retorna um vetor vazio
			if (!page.hasNext()) {
				return new ArrayList<>();
			}

			// Lê a próxima folha
			page = readPage(page.getNext());

			// Testa se a chave é a primeira da próxima folha
			i = 0;
			if (elem.compareTo(page.registers.get(i)) <= 0) {

				// Cria a lista de retorno
				ArrayList<BPlusRegister> lista = new ArrayList<>();

				// Testa se a chave foi encontrada, e adiciona todas as chaves
				// secundárias
				while (elem.compareTo(page.registers.get(i)) <= 0) {
					if (elem.compareTo(page.registers.get(i)) == 0) {
						lista.add(page.registers.get(i));
					}

					i++;
					if (i == page.getRegistersSize()) {
						if (page.getNext() == -1) {
							break;
						}

						page = readPage(page.getNext());

						i = 0;
					}
				}

				return lista;
			}

			// Se não houver uma próxima página, retorna um vetor vazio
			else {
				return new ArrayList<>();
			}
		}

		// Chave ainda não foi encontrada, continua a busca recursiva pela árvore
		if (elem == null || i == page.getRegistersSize() || elem.compareTo(page.registers.get(i)) <= 0) {
			return read(elem, page.childrens.get(i));
		} else {
			return read(elem, page.childrens.get(i + 1));
		}
	}

	@Override
	public void add(T register, long position) throws IOException {
		int id = register.getId();

		BPlusRegister bPlusregister = new BPlusRegister(id, position);

		long pagina = this.getRoot();

		this.auxRegister = bPlusregister.clone();
		this.auxPagePosition = -1;
		this.pageGrew = false;

		// Chamada recursiva para a inserção do par de chaves
		create(pagina);

		// Testa a necessidade de criação de uma nova raiz.
		if (this.pageGrew) {

			// Cria a nova página que será a raiz. O ponteiro esquerdo da raiz
			// será a raiz antiga e o seu ponteiro direito será para a nova página.
			BPlusPage novaBPlusPage = new BPlusPage(this.order);
			novaBPlusPage.addRegister(this.auxRegister);
			novaBPlusPage.addChild(pagina);
			novaBPlusPage.addChild(auxPagePosition);

			// Acha o espaço em disco. Testa se há páginas excluídas.
			this.file.seek(8);
			long end = this.file.readLong();
			if (end == -1) {
				end = this.file.length();
			} else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
				BPlusPage pa_excluida = readPage(end);

				this.file.seek(8);
				this.file.writeLong(pa_excluida.getNext());
			}

			this.file.seek(end);
			long raiz = this.file.getFilePointer();
			this.file.write(novaBPlusPage.toByteArray());
			this.file.seek(0);
			this.file.writeLong(raiz);
		}
	}

	// Função recursiva de inclusão. A função passa uma página de referência.
	// As inclusões são sempre feitas em uma folha.
	private boolean create(long pagina) throws IOException {

		// Testa se passou para o filho de uma página folha. Nesse caso,
		// inicializa as variáveis globais de controle.
		if (pagina == -1) {
			this.pageGrew = true;
			auxPagePosition = -1;
			return false;
		}

		// Lê a página passada como referência
		BPlusPage pa = readPage(pagina);

		// Busca o próximo ponteiro de descida. Como pode haver repetição
		// da primeira chave, a segunda também é usada como referência.
		// Nesse primeiro passo, todos os pares menores são ultrapassados.
		int i = 0;
		while (i < pa.getRegistersSize() && (this.auxRegister.compareTo(pa.registers.get(i)) > 0)) {
			i++;
		}

		// Testa se o registro já existe em uma folha. Se isso acontecer, então
		// a inclusão é cancelada.
		if (i < pa.getRegistersSize() && pa.isLeaf() && this.auxRegister.compareTo(pa.registers.get(i)) == 0) {
			this.pageGrew = false;
			return false;
		}

		// Continua a busca recursiva por uma nova página. A busca continuará até o
		// filho inexistente de uma página folha ser alcançado.
		boolean inserido;
		if (i == pa.getRegistersSize() || this.auxRegister.compareTo(pa.registers.get(i)) < 0) {
			inserido = create(pa.childrens.get(i));
		} else {
			inserido = create(pa.childrens.get(i + 1));
		}

		if (!this.pageGrew) {
			return inserido;
		}

		// Se tiver espaço na página, faz a inclusão nela mesmo
		if (!pa.isFull()) {

			// Puxa todos elementos para a direita, começando do último
			// para gerar o espaço para o novo elemento e insere o novo elemento
			pa.registers.add(i, this.auxRegister);
			pa.childrens.add(i + 1, auxPagePosition);

			// Escreve a página atualizada no this.file
			this.file.seek(pagina);
			this.file.write(pa.toByteArray());

			// Encerra o processo de crescimento e retorna
			this.pageGrew = false;
			return true;
		}

		// O elemento não cabe na página. A página deve ser dividida e o elemento
		// do meio deve ser promovido (sem retirar a referência da folha).

		// Cria uma nova página
		BPlusPage np = new BPlusPage(this.order);

		// Move a metade superior dos elementos para a nova página,
		// considerando que maxElementos pode ser ímpar
		int meio = BPlusPage.getMaxRegisters(this.order) / 2;
		np.childrens.add(pa.childrens.get(meio)); // COPIA o primeiro ponteiro
		for (int j = 0; j < (BPlusPage.getMaxRegisters(this.order) - meio); j++) {
			np.registers.add(pa.registers.remove(meio)); // MOVE os elementos
			np.childrens.add(pa.childrens.remove(meio + 1)); // MOVE os demais ponteiros
		}

		// Testa o lado de inserção
		// Caso 1 - Novo registro deve ficar na página da esquerda
		if (i <= meio) {
			pa.registers.add(i, this.auxRegister);
			pa.childrens.add(i + 1, auxPagePosition);

			// Se a página for folha, seleciona o primeiro elemento da página
			// da direita para ser promovido, mantendo-o na folha
			if (pa.isLeaf()) {
				this.auxRegister = np.registers.get(0).clone();
			}

			// caso contrário, promove o maior elemento da página esquerda
			// removendo-o da página
			else {
				this.auxRegister = pa.registers.remove(pa.getRegistersSize() - 1);
				pa.childrens.remove(pa.childrens.size() - 1);
			}
		}

		// Caso 2 - Novo registro deve ficar na página da direita
		else {

			int j = BPlusPage.getMaxRegisters(this.order) - meio;
			while (this.auxRegister.compareTo(np.registers.get(j - 1)) < 0) {
				j--;
			}
			np.registers.add(j, this.auxRegister);
			np.childrens.add(j + 1, auxPagePosition);

			// Seleciona o primeiro elemento da página da direita para ser promovido
			this.auxRegister = np.registers.get(0).clone();

			// Se não for folha, remove o elemento promovido da página
			if (!pa.isLeaf()) {
				np.registers.remove(0);
				np.childrens.remove(0);
			}
		}

		// Obtém um endereço para a nova página (página excluída ou fim do this.file)
		this.file.seek(8);
		long end = this.file.readLong();
		if (end == -1) {
			end = this.file.length();
		} else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
			BPlusPage pa_excluida = readPage(end);

			this.file.seek(8);
			this.file.writeLong(pa_excluida.getNext());
		}

		// Se a página era uma folha e apontava para outra folha,
		// então atualiza os ponteiros dessa página e da página nova
		if (pa.isLeaf()) {
			np.setNext(pa.getNext());
			pa.setNext(end);
		}

		// Grava as páginas no this.file
		auxPagePosition = end;
		this.file.seek(auxPagePosition);
		this.file.write(np.toByteArray());

		this.file.seek(pagina);
		this.file.write(pa.toByteArray());

		return true;
	}

	@Override
	public void remove(T register) throws IOException {
		int id = register.getId();

		BPlusRegister bPlusregister = new BPlusRegister(id, 0);
		delete(bPlusregister);
	}

	// Remoção elementos na árvore. A remoção é recursiva. A primeira
	// função chama a segunda recursivamente, passando a raiz como referência.
	// Eventualmente, a árvore pode reduzir seu tamanho, por meio da exclusão da
	// raiz.
	public boolean delete(BPlusRegister elem) throws IOException {

		// Encontra a raiz da árvore
		this.file.seek(0);
		long pagina;
		pagina = this.file.readLong();

		// variável global de controle da redução do tamanho da árvore
		this.pageShrunk = false;

		// Chama recursivamente a exclusão de registro (na auxRegister e no
		// chave2Aux) passando uma página como referência
		boolean excluido = delete(elem, pagina);

		// Se a exclusão tiver sido possível e a página tiver reduzido seu tamanho,
		// por meio da fusão das duas páginas filhas da raiz, elimina essa raiz
		if (excluido && this.pageShrunk) {

			// Lê a raiz
			BPlusPage pa = readPage(pagina);

			// Se a página tiver 0 elementos, apenas atualiza o ponteiro para a raiz,
			// no cabeçalho do this.file, para o seu primeiro filho e insere a raiz velha
			// na lista de páginas excluídas
			if (pa.getRegistersSize() == 0) {
				this.file.seek(0);
				this.file.writeLong(pa.childrens.get(0));

				this.file.seek(8);
				long end = this.file.readLong(); // cabeça da lista de páginas excluídas
				pa.setNext(end);
				this.file.seek(8);
				this.file.writeLong(pagina);
				this.file.seek(pagina);
				this.file.write(pa.toByteArray());
			}
		}

		return excluido;
	}

	// Função recursiva de exclusão. A função passa uma página de referência.
	// As exclusões são sempre feitas em folhas e a fusão é propagada para cima.
	private boolean delete(BPlusRegister elem, long pagina) throws IOException {

		// Declaração de variáveis
		boolean excluido = false;
		int diminuido;

		// Testa se o registro não foi encontrado na árvore, ao alcançar uma folha
		// inexistente (filho de uma folha real)
		if (pagina == -1) {
			this.pageShrunk = false;
			return false;
		}

		// Lê o registro da página no this.file
		BPlusPage pa = readPage(pagina);

		// Encontra a página em que o par de chaves está presente
		// Nesse primeiro passo, salta todas os pares de chaves menores
		int i = 0;
		while (i < pa.getRegistersSize() && elem.compareTo(pa.registers.get(i)) > 0) {
			i++;
		}

		// Chaves encontradas em uma folha
		if (i < pa.getRegistersSize() && pa.isLeaf() && elem.compareTo(pa.registers.get(i)) == 0) {

			// Puxa todas os elementos seguintes para uma posição anterior, sobrescrevendo
			// o elemento a ser excluído
			pa.registers.remove(i);
			pa.childrens.remove(i + 1);

			// Atualiza o registro da página no this.file
			this.file.seek(pagina);
			this.file.write(pa.toByteArray());

			// Se a página contiver menos elementos do que o mínimo necessário,
			// indica a necessidade de fusão de páginas
			this.pageShrunk = pa.getRegistersSize() < BPlusPage.getMaxRegisters(this.order) / 2;
			return true;
		}

		// Se a chave não tiver sido encontrada (observar o return true logo acima),
		// continua a busca recursiva por uma nova página. A busca continuará até o
		// filho inexistente de uma página folha ser alcançado.
		// A variável diminuído mantem um registro de qual página eventualmente
		// pode ter ficado com menos elementos do que o mínimo necessário.
		// Essa página será filha da página atual
		if (i == pa.getRegistersSize() || elem.compareTo(pa.registers.get(i)) < 0) {
			excluido = delete(elem, pa.childrens.get(i));
			diminuido = i;
		} else {
			excluido = delete(elem, pa.childrens.get(i + 1));
			diminuido = i + 1;
		}

		// A partir deste ponto, o código é executado após o retorno das chamadas
		// recursivas do método

		// Testa se há necessidade de fusão de páginas
		if (this.pageShrunk) {

			// Carrega a página filho que ficou com menos elementos do
			// do que o mínimo necessário
			long paginaFilho = pa.childrens.get(diminuido);

			BPlusPage pFilho = readPage(paginaFilho);

			// Cria uma página para o irmão (da direita ou esquerda)
			long paginaIrmaoEsq = -1, paginaIrmaoDir = -1;
			BPlusPage pIrmaoEsq = null, pIrmaoDir = null; // inicializados com null para controle de existência

			// Carrega os irmãos (que existirem)
			if (diminuido > 0) { // possui um irmão esquerdo, pois não é a primeira filho do pai
				paginaIrmaoEsq = pa.childrens.get(diminuido - 1);

				pIrmaoEsq = readPage(paginaIrmaoEsq);
			}

			if (diminuido < pa.getRegistersSize()) { // possui um irmão direito, pois não é o último filho do pai
				paginaIrmaoDir = pa.childrens.get(diminuido + 1);

				pIrmaoDir = readPage(paginaIrmaoDir);
			}

			// Verifica se o irmão esquerdo existe e pode ceder algum elemento
			if (pIrmaoEsq != null && pIrmaoEsq.getRegistersSize() > BPlusPage.getMaxRegisters(this.order) / 2) {

				// Se for folha, copia o elemento do irmão, já que o do pai será extinto ou
				// repetido
				if (pFilho.isLeaf()) {
					pFilho.registers.add(0, pIrmaoEsq.registers.remove(pIrmaoEsq.getRegistersSize() - 1));
				}

				// Se não for folha, desce o elemento do pai
				else {
					pFilho.registers.add(0, pa.registers.get(diminuido - 1));
				}

				// Copia o elemento vindo do irmão para o pai (página atual)
				pa.registers.set(diminuido - 1, pFilho.registers.get(0));

				// Reduz o elemento no irmão
				pFilho.childrens.add(0, pIrmaoEsq.childrens.remove(pIrmaoEsq.childrens.size() - 1));
			}

			// Senão, verifica se o irmão direito existe e pode ceder algum elemento
			else if (pIrmaoDir != null && pIrmaoDir.getRegistersSize() > BPlusPage.getMaxRegisters(this.order) / 2) {
				// Se for folha
				if (pFilho.isLeaf()) {

					// move o elemento do irmão
					pFilho.registers.add(pIrmaoDir.registers.remove(0));
					pFilho.childrens.add(pIrmaoDir.childrens.remove(0));

					// sobe o próximo elemento do irmão
					pa.registers.set(diminuido, pIrmaoDir.registers.get(0));
				}

				// Se não for folha, rotaciona os elementos
				else {
					// Copia o elemento do pai, com o ponteiro esquerdo do irmão
					pFilho.registers.add(pa.registers.get(diminuido));
					pFilho.childrens.add(pIrmaoDir.childrens.remove(0));

					// Sobe o elemento esquerdo do irmão para o pai
					pa.registers.set(diminuido, pIrmaoDir.registers.remove(0));
				}
			}

			// Senão, faz a fusão com o irmão esquerdo, se ele existir
			else if (pIrmaoEsq != null) {
				// Se a página reduzida não for folha, então o elemento
				// do pai deve descer para o irmão
				if (!pFilho.isLeaf()) {
					pIrmaoEsq.registers.add(pa.registers.remove(diminuido - 1));
					pIrmaoEsq.childrens.add(pFilho.childrens.remove(0));
				}
				// Senão, apenas remove o elemento do pai
				else {
					pa.registers.remove(diminuido - 1);
					pFilho.childrens.remove(0);
				}
				pa.childrens.remove(diminuido); // remove o ponteiro para a própria página

				// Copia todos os registros para o irmão da esquerda
				pIrmaoEsq.registers.addAll(pFilho.registers);
				pIrmaoEsq.childrens.addAll(pFilho.childrens);
				pFilho.registers.clear();
				pFilho.childrens.clear();

				// Se as páginas forem folhas, copia o ponteiro para a folha seguinte
				if (pIrmaoEsq.isLeaf()) {
					pIrmaoEsq.setNext(pFilho.getNext());
				}

				// Insere o filho na lista de páginas excluídas
				this.file.seek(8);
				pFilho.setNext(this.file.readLong());
				this.file.seek(8);
				this.file.writeLong(paginaFilho);
			}

			// Senão, faz a fusão com o irmão direito, assumindo que ele existe
			else {
				// Se a página reduzida não for folha, então o elemento
				// do pai deve descer para o irmão
				if (!pFilho.isLeaf()) {
					pFilho.registers.add(pa.registers.remove(diminuido));
					pFilho.childrens.add(pIrmaoDir.childrens.remove(0));
				}
				// Senão, apenas remove o elemento do pai
				else {
					pa.registers.remove(diminuido);
					pFilho.childrens.remove(0);
				}
				pa.childrens.remove(diminuido + 1); // remove o ponteiro para o irmão direito

				// Move todos os registros do irmão da direita
				pFilho.registers.addAll(pIrmaoDir.registers);
				pFilho.childrens.addAll(pIrmaoDir.childrens);
				pIrmaoDir.registers.clear();
				pIrmaoDir.childrens.clear();

				// Se a página for folha, copia o ponteiro para a próxima página
				pFilho.setNext(pIrmaoDir.getNext());

				// Insere o irmão da direita na lista de páginas excluídas
				this.file.seek(8);
				pIrmaoDir.setNext(this.file.readLong());
				this.file.seek(8);
				this.file.writeLong(paginaIrmaoDir);
			}

			// testa se o pai também ficou sem o número mínimo de elementos
			this.pageShrunk = pa.getRegistersSize() < BPlusPage.getMaxRegisters(this.order) / 2;

			// Atualiza os demais registros
			this.file.seek(pagina);
			this.file.write(pa.toByteArray());
			this.file.seek(paginaFilho);
			this.file.write(pFilho.toByteArray());
			if (pIrmaoEsq != null) {
				this.file.seek(paginaIrmaoEsq);
				this.file.write(pIrmaoEsq.toByteArray());
			}

			if (pIrmaoDir != null) {
				this.file.seek(paginaIrmaoDir);
				this.file.write(pIrmaoDir.toByteArray());
			}
		}

		return excluido;
	}

	@Override
	public void clear() throws IOException {
		this.file.setLength(0);
		this.file.writeLong(-1);
		this.file.writeLong(-1);
	}

	private BPlusPage readPage(long position) throws IOException {
		this.file.seek(position);
		byte[] buffer = new byte[BPlusPage.getSize(this.order)];
		this.file.read(buffer);

		return new BPlusPage(buffer, this.order);
	}

	@Override
	public String getName() {
		return "Árvore B+";
	}
}

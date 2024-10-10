package index.bplustree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import db.Database;
import index.IndexStrategy;

public class BPlusTree implements IndexStrategy {
	private int order;
	private String filePath;
	private RandomAccessFile file;


	private int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
	private int maxFilhos; // Variável igual a ordem para facilitar a clareza do código

	// Variáveis usadas nas funções recursivas (já que não é possível passar valores
	// por referência)
	private BPlusRegister elemAux;
	private long paginaAux;
	private boolean cresceu;
	private boolean diminuiu;

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
    this.filePath = filePath + "BPlusTreeIndex" + Database.getFileExtension();
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


	// constructor
	public BPlusTree(int order, String filePath) throws Exception {
		this.setOrder(order);
		this.setFilePath(filePath);

		this.setFile();

		maxElementos = this.getOrder() - 1;	// TODO: remove
		maxFilhos = this.getOrder();				// TODO: remove
	}

	// Testa se a árvore está empty. Uma árvore empty é identificada pela raiz == -1
	public boolean empty() throws IOException {
		long raiz;
		this.file.seek(0);
		raiz = this.file.readLong();
		return raiz == -1;
	}

	@Override
	public long get(int id) throws IOException {
		BPlusRegister register = new BPlusRegister(id, 0);
		ArrayList<BPlusRegister> registers = read(register);

		if (registers.isEmpty()) {
			return -1;
		}

		return registers.get(0).getPosition();
	}

	// Busca recursiva por um elemento a partir da chave. Este metodo invoca
	// o método recursivo read1, passando a raiz como referência.
	// O método retorna a lista de elementos que possuem a chave (considerando
	// a possibilidade chaves repetidas)
	public ArrayList<BPlusRegister> read(BPlusRegister elem) throws IOException {

		// Recupera a raiz da árvore
		long raiz;
		this.file.seek(0);
		raiz = this.file.readLong();

		// Executa a busca recursiva
		if (raiz != -1)
			return read(elem, raiz);
		else {
			ArrayList<BPlusRegister> resposta = new ArrayList<>();
			return resposta;
		}
	}

	// Busca recursiva. Este método recebe a referência de uma página e busca
	// pela chave na mesma. A busca continua pelos filhos, se houverem.
	private ArrayList<BPlusRegister> read(BPlusRegister elem, long pagina) throws IOException {

		// Como a busca é recursiva, a descida para um filho inexistente
		// (filho de uma página folha) retorna um vetor vazio.
		if (pagina == -1) {
			ArrayList<BPlusRegister> resposta = new ArrayList<>();
			return resposta;
		}

		// Reconstrói a página passada como referência a partir
		// do registro lido no this.file
		this.file.seek(pagina);
		BPlusPage pa = new BPlusPage(this.order);
		byte[] buffer = new byte[pa.TAMANHO_PAGINA];
		this.file.read(buffer);
		pa.fromByteArray(buffer);

		// Encontra o ponto em que a chave deve estar na página
		// Nesse primeiro passo, todas as chaves menores que a chave buscada
		// são ultrapassadas
		int i = 0;
		while (elem != null && i < pa.elementos.size() && elem.compareTo(pa.elementos.get(i)) > 0) {
			i++;
		}

		// Chave encontrada (ou pelo menos o ponto onde ela deveria estar).
		// Segundo passo - testa se a chave é a chave buscada e se está em uma folha
		// Obs.: em uma árvore B+, todas as chaves válidas estão nas folhas
		if (i < pa.elementos.size() && pa.filhos.get(0) == -1
				&& (elem == null || elem.compareTo(pa.elementos.get(i)) == 0)) {

			// Cria a lista de retorno e insere os elementos encontrados
			ArrayList<BPlusRegister> lista = new ArrayList<>();
			while (elem == null || elem.compareTo(pa.elementos.get(i)) <= 0) {

				if (elem == null || elem.compareTo(pa.elementos.get(i)) == 0)
					lista.add(pa.elementos.get(i));
				i++;

				// Se chegar ao fim da folha, então avança para a folha seguinte
				if (i == pa.elementos.size()) {
					if (pa.proxima == -1)
						break;
					this.file.seek(pa.proxima);
					this.file.read(buffer);
					pa.fromByteArray(buffer);
					i = 0;
				}
			}
			return lista;
		}

		// Terceiro passo - se a chave não tiver sido encontrada nesta folha,
		// testa se ela está na próxima folha. Isso pode ocorrer devido ao
		// processo de ordenação.
		else if (i == pa.elementos.size() && pa.filhos.get(0) == -1) {

			// Testa se há uma próxima folha. Nesse caso, retorna um vetor vazio
			if (pa.proxima == -1) {
				ArrayList<BPlusRegister> resposta = new ArrayList<>();
				return resposta;
			}

			// Lê a próxima folha
			this.file.seek(pa.proxima);
			this.file.read(buffer);
			pa.fromByteArray(buffer);

			// Testa se a chave é a primeira da próxima folha
			i = 0;
			if (elem.compareTo(pa.elementos.get(i)) <= 0) {

				// Cria a lista de retorno
				ArrayList<BPlusRegister> lista = new ArrayList<>();

				// Testa se a chave foi encontrada, e adiciona todas as chaves
				// secundárias
				while (elem.compareTo(pa.elementos.get(i)) <= 0) {
					if (elem.compareTo(pa.elementos.get(i)) == 0)
						lista.add(pa.elementos.get(i));
					i++;
					if (i == pa.elementos.size()) {
						if (pa.proxima == -1)
							break;
						this.file.seek(pa.proxima);
						this.file.read(buffer);
						pa.fromByteArray(buffer);
						i = 0;
					}
				}

				return lista;
			}

			// Se não houver uma próxima página, retorna um vetor vazio
			else {
				ArrayList<BPlusRegister> resposta = new ArrayList<>();
				return resposta;
			}
		}

		// Chave ainda não foi encontrada, continua a busca recursiva pela árvore
		if (elem == null || i == pa.elementos.size() || elem.compareTo(pa.elementos.get(i)) <= 0)
			return read(elem, pa.filhos.get(i));
		else
			return read(elem, pa.filhos.get(i + 1));
	}

	@Override
	public void add(int id, long position) throws IOException {
		BPlusRegister register = new BPlusRegister(id, position);
		create(register);
	}

	// Inclusão de novos elementos na árvore. A inclusão é recursiva. A primeira
	// função chama a segunda recursivamente, passando a raiz como referência.
	// Eventualmente, a árvore pode crescer para cima.
	public boolean create(BPlusRegister elem) throws IOException {

		// Carrega a raiz
		this.file.seek(0);
		long pagina;
		pagina = this.file.readLong();

		// O processo de inclusão permite que os valores passados como referência
		// sejam substituídos por outros valores, para permitir a divisão de páginas
		// e crescimento da árvore. Assim, são usados os valores globais elemAux
		// e chave2Aux. Quando há uma divisão, as chaves promovidas são armazenadas
		// nessas variáveis.
		elemAux = elem.clone();

		// Se houver crescimento, então será criada uma página extra e será mantido um
		// ponteiro para essa página. Os valores também são globais.
		paginaAux = -1;
		cresceu = false;

		// Chamada recursiva para a inserção do par de chaves
		boolean inserido = create(pagina);

		// Testa a necessidade de criação de uma nova raiz.
		if (cresceu) {

			// Cria a nova página que será a raiz. O ponteiro esquerdo da raiz
			// será a raiz antiga e o seu ponteiro direito será para a nova página.
			BPlusPage novaBPlusPage = new BPlusPage(this.order);
			novaBPlusPage.elementos = new ArrayList<>(this.maxElementos);
			novaBPlusPage.elementos.add(elemAux);
			novaBPlusPage.filhos = new ArrayList<>(this.maxFilhos);
			novaBPlusPage.filhos.add(pagina);
			novaBPlusPage.filhos.add(paginaAux);

			// Acha o espaço em disco. Testa se há páginas excluídas.
			this.file.seek(8);
			long end = this.file.readLong();
			if (end == -1) {
				end = this.file.length();
			} else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
				this.file.seek(end);
				BPlusPage pa_excluida = new BPlusPage(this.order);
				byte[] buffer = new byte[pa_excluida.TAMANHO_PAGINA];
				this.file.read(buffer);
				pa_excluida.fromByteArray(buffer);
				this.file.seek(8);
				this.file.writeLong(pa_excluida.proxima);
			}
			this.file.seek(end);
			long raiz = this.file.getFilePointer();
			this.file.write(novaBPlusPage.toByteArray());
			this.file.seek(0);
			this.file.writeLong(raiz);
			inserido = true;
		}

		return inserido;
	}

	// Função recursiva de inclusão. A função passa uma página de referência.
	// As inclusões são sempre feitas em uma folha.
	private boolean create(long pagina) throws IOException {

		// Testa se passou para o filho de uma página folha. Nesse caso,
		// inicializa as variáveis globais de controle.
		if (pagina == -1) {
			cresceu = true;
			paginaAux = -1;
			return false;
		}

		// Lê a página passada como referência
		this.file.seek(pagina);
		BPlusPage pa = new BPlusPage(this.order);
		byte[] buffer = new byte[pa.TAMANHO_PAGINA];
		this.file.read(buffer);
		pa.fromByteArray(buffer);

		// Busca o próximo ponteiro de descida. Como pode haver repetição
		// da primeira chave, a segunda também é usada como referência.
		// Nesse primeiro passo, todos os pares menores são ultrapassados.
		int i = 0;
		while (i < pa.elementos.size() && (elemAux.compareTo(pa.elementos.get(i)) > 0)) {
			i++;
		}

		// Testa se o registro já existe em uma folha. Se isso acontecer, então
		// a inclusão é cancelada.
		if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && elemAux.compareTo(pa.elementos.get(i)) == 0) {
			cresceu = false;
			return false;
		}

		// Continua a busca recursiva por uma nova página. A busca continuará até o
		// filho inexistente de uma página folha ser alcançado.
		boolean inserido;
		if (i == pa.elementos.size() || elemAux.compareTo(pa.elementos.get(i)) < 0)
			inserido = create(pa.filhos.get(i));
		else
			inserido = create(pa.filhos.get(i + 1));

		// A partir deste ponto, as chamadas recursivas já foram encerradas.
		// Assim, o próximo código só é executado ao retornar das chamadas recursivas.

		// A inclusão já foi resolvida por meio de uma das chamadas recursivas. Nesse
		// caso, apenas retorna para encerrar a recursão.
		// A inclusão pode ter sido resolvida porque o par de chaves já existia
		// (inclusão inválida)
		// ou porque o novo elemento coube em uma página existente.
		if (!cresceu)
			return inserido;

		// Se tiver espaço na página, faz a inclusão nela mesmo
		if (pa.elementos.size() < maxElementos) {

			// Puxa todos elementos para a direita, começando do último
			// para gerar o espaço para o novo elemento e insere o novo elemento
			pa.elementos.add(i, elemAux);
			pa.filhos.add(i + 1, paginaAux);

			// Escreve a página atualizada no this.file
			this.file.seek(pagina);
			this.file.write(pa.toByteArray());

			// Encerra o processo de crescimento e retorna
			cresceu = false;
			return true;
		}

		// O elemento não cabe na página. A página deve ser dividida e o elemento
		// do meio deve ser promovido (sem retirar a referência da folha).

		// Cria uma nova página
		BPlusPage np = new BPlusPage(this.order);

		// Move a metade superior dos elementos para a nova página,
		// considerando que maxElementos pode ser ímpar
		int meio = maxElementos / 2;
		np.filhos.add(pa.filhos.get(meio)); // COPIA o primeiro ponteiro
		for (int j = 0; j < (maxElementos - meio); j++) {
			np.elementos.add(pa.elementos.remove(meio)); // MOVE os elementos
			np.filhos.add(pa.filhos.remove(meio + 1)); // MOVE os demais ponteiros
		}

		// Testa o lado de inserção
		// Caso 1 - Novo registro deve ficar na página da esquerda
		if (i <= meio) {
			pa.elementos.add(i, elemAux);
			pa.filhos.add(i + 1, paginaAux);

			// Se a página for folha, seleciona o primeiro elemento da página
			// da direita para ser promovido, mantendo-o na folha
			if (pa.filhos.get(0) == -1)
				elemAux = np.elementos.get(0).clone();

			// caso contrário, promove o maior elemento da página esquerda
			// removendo-o da página
			else {
				elemAux = pa.elementos.remove(pa.elementos.size() - 1);
				pa.filhos.remove(pa.filhos.size() - 1);
			}
		}

		// Caso 2 - Novo registro deve ficar na página da direita
		else {

			int j = maxElementos - meio;
			while (elemAux.compareTo(np.elementos.get(j - 1)) < 0)
				j--;
			np.elementos.add(j, elemAux);
			np.filhos.add(j + 1, paginaAux);

			// Seleciona o primeiro elemento da página da direita para ser promovido
			elemAux = np.elementos.get(0).clone();

			// Se não for folha, remove o elemento promovido da página
			if (pa.filhos.get(0) != -1) {
				np.elementos.remove(0);
				np.filhos.remove(0);
			}

		}

		// Obtém um endereço para a nova página (página excluída ou fim do this.file)
		this.file.seek(8);
		long end = this.file.readLong();
		if (end == -1) {
			end = this.file.length();
		} else { // reusa um endereço e atualiza a lista de excluídos no cabeçalho
			this.file.seek(end);
			BPlusPage pa_excluida = new BPlusPage(this.order);
			buffer = new byte[pa_excluida.TAMANHO_PAGINA];
			this.file.read(buffer);
			pa_excluida.fromByteArray(buffer);
			this.file.seek(8);
			this.file.writeLong(pa_excluida.proxima);
		}

		// Se a página era uma folha e apontava para outra folha,
		// então atualiza os ponteiros dessa página e da página nova
		if (pa.filhos.get(0) == -1) {
			np.proxima = pa.proxima;
			pa.proxima = end;
		}

		// Grava as páginas no this.file
		paginaAux = end;
		this.file.seek(paginaAux);
		this.file.write(np.toByteArray());

		this.file.seek(pagina);
		this.file.write(pa.toByteArray());

		return true;
	}

	@Override
	public void remove(int id) throws IOException {
		BPlusRegister register = new BPlusRegister(id, 0);
		delete(register);
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
		diminuiu = false;

		// Chama recursivamente a exclusão de registro (na elemAux e no
		// chave2Aux) passando uma página como referência
		boolean excluido = delete(elem, pagina);

		// Se a exclusão tiver sido possível e a página tiver reduzido seu tamanho,
		// por meio da fusão das duas páginas filhas da raiz, elimina essa raiz
		if (excluido && diminuiu) {

			// Lê a raiz
			this.file.seek(pagina);
			BPlusPage pa = new BPlusPage(this.order);
			byte[] buffer = new byte[pa.TAMANHO_PAGINA];
			this.file.read(buffer);
			pa.fromByteArray(buffer);

			// Se a página tiver 0 elementos, apenas atualiza o ponteiro para a raiz,
			// no cabeçalho do this.file, para o seu primeiro filho e insere a raiz velha
			// na lista de páginas excluídas
			if (pa.elementos.size() == 0) {
				this.file.seek(0);
				this.file.writeLong(pa.filhos.get(0));

				this.file.seek(8);
				long end = this.file.readLong(); // cabeça da lista de páginas excluídas
				pa.proxima = end;
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
			diminuiu = false;
			return false;
		}

		// Lê o registro da página no this.file
		this.file.seek(pagina);
		BPlusPage pa = new BPlusPage(this.order);
		byte[] buffer = new byte[pa.TAMANHO_PAGINA];
		this.file.read(buffer);
		pa.fromByteArray(buffer);

		// Encontra a página em que o par de chaves está presente
		// Nesse primeiro passo, salta todas os pares de chaves menores
		int i = 0;
		while (i < pa.elementos.size() && elem.compareTo(pa.elementos.get(i)) > 0) {
			i++;
		}

		// Chaves encontradas em uma folha
		if (i < pa.elementos.size() && pa.filhos.get(0) == -1 && elem.compareTo(pa.elementos.get(i)) == 0) {

			// Puxa todas os elementos seguintes para uma posição anterior, sobrescrevendo
			// o elemento a ser excluído
			pa.elementos.remove(i);
			pa.filhos.remove(i + 1);

			// Atualiza o registro da página no this.file
			this.file.seek(pagina);
			this.file.write(pa.toByteArray());

			// Se a página contiver menos elementos do que o mínimo necessário,
			// indica a necessidade de fusão de páginas
			diminuiu = pa.elementos.size() < maxElementos / 2;
			return true;
		}

		// Se a chave não tiver sido encontrada (observar o return true logo acima),
		// continua a busca recursiva por uma nova página. A busca continuará até o
		// filho inexistente de uma página folha ser alcançado.
		// A variável diminuído mantem um registro de qual página eventualmente
		// pode ter ficado com menos elementos do que o mínimo necessário.
		// Essa página será filha da página atual
		if (i == pa.elementos.size() || elem.compareTo(pa.elementos.get(i)) < 0) {
			excluido = delete(elem, pa.filhos.get(i));
			diminuido = i;
		} else {
			excluido = delete(elem, pa.filhos.get(i + 1));
			diminuido = i + 1;
		}

		// A partir deste ponto, o código é executado após o retorno das chamadas
		// recursivas do método

		// Testa se há necessidade de fusão de páginas
		if (diminuiu) {

			// Carrega a página filho que ficou com menos elementos do
			// do que o mínimo necessário
			long paginaFilho = pa.filhos.get(diminuido);
			BPlusPage pFilho = new BPlusPage(this.order);
			this.file.seek(paginaFilho);
			this.file.read(buffer);
			pFilho.fromByteArray(buffer);

			// Cria uma página para o irmão (da direita ou esquerda)
			long paginaIrmaoEsq = -1, paginaIrmaoDir = -1;
			BPlusPage pIrmaoEsq = null, pIrmaoDir = null; // inicializados com null para controle de existência

			// Carrega os irmãos (que existirem)
			if (diminuido > 0) { // possui um irmão esquerdo, pois não é a primeira filho do pai
				paginaIrmaoEsq = pa.filhos.get(diminuido - 1);
				pIrmaoEsq = new BPlusPage(this.order);
				this.file.seek(paginaIrmaoEsq);
				this.file.read(buffer);
				pIrmaoEsq.fromByteArray(buffer);
			}
			if (diminuido < pa.elementos.size()) { // possui um irmão direito, pois não é o último filho do pai
				paginaIrmaoDir = pa.filhos.get(diminuido + 1);
				pIrmaoDir = new BPlusPage(this.order);
				this.file.seek(paginaIrmaoDir);
				this.file.read(buffer);
				pIrmaoDir.fromByteArray(buffer);
			}

			// Verifica se o irmão esquerdo existe e pode ceder algum elemento
			if (pIrmaoEsq != null && pIrmaoEsq.elementos.size() > maxElementos / 2) {

				// Se for folha, copia o elemento do irmão, já que o do pai será extinto ou
				// repetido
				if (pFilho.filhos.get(0) == -1)
					pFilho.elementos.add(0, pIrmaoEsq.elementos.remove(pIrmaoEsq.elementos.size() - 1));

				// Se não for folha, desce o elemento do pai
				else
					pFilho.elementos.add(0, pa.elementos.get(diminuido - 1));

				// Copia o elemento vindo do irmão para o pai (página atual)
				pa.elementos.set(diminuido - 1, pFilho.elementos.get(0));

				// Reduz o elemento no irmão
				pFilho.filhos.add(0, pIrmaoEsq.filhos.remove(pIrmaoEsq.filhos.size() - 1));

			}

			// Senão, verifica se o irmão direito existe e pode ceder algum elemento
			else if (pIrmaoDir != null && pIrmaoDir.elementos.size() > maxElementos / 2) {
				// Se for folha
				if (pFilho.filhos.get(0) == -1) {

					// move o elemento do irmão
					pFilho.elementos.add(pIrmaoDir.elementos.remove(0));
					pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

					// sobe o próximo elemento do irmão
					pa.elementos.set(diminuido, pIrmaoDir.elementos.get(0));
				}

				// Se não for folha, rotaciona os elementos
				else {
					// Copia o elemento do pai, com o ponteiro esquerdo do irmão
					pFilho.elementos.add(pa.elementos.get(diminuido));
					pFilho.filhos.add(pIrmaoDir.filhos.remove(0));

					// Sobe o elemento esquerdo do irmão para o pai
					pa.elementos.set(diminuido, pIrmaoDir.elementos.remove(0));
				}
			}

			// Senão, faz a fusão com o irmão esquerdo, se ele existir
			else if (pIrmaoEsq != null) {
				// Se a página reduzida não for folha, então o elemento
				// do pai deve descer para o irmão
				if (pFilho.filhos.get(0) != -1) {
					pIrmaoEsq.elementos.add(pa.elementos.remove(diminuido - 1));
					pIrmaoEsq.filhos.add(pFilho.filhos.remove(0));
				}
				// Senão, apenas remove o elemento do pai
				else {
					pa.elementos.remove(diminuido - 1);
					pFilho.filhos.remove(0);
				}
				pa.filhos.remove(diminuido); // remove o ponteiro para a própria página

				// Copia todos os registros para o irmão da esquerda
				pIrmaoEsq.elementos.addAll(pFilho.elementos);
				pIrmaoEsq.filhos.addAll(pFilho.filhos);
				pFilho.elementos.clear();
				pFilho.filhos.clear();

				// Se as páginas forem folhas, copia o ponteiro para a folha seguinte
				if (pIrmaoEsq.filhos.get(0) == -1)
					pIrmaoEsq.proxima = pFilho.proxima;

				// Insere o filho na lista de páginas excluídas
				this.file.seek(8);
				pFilho.proxima = this.file.readLong();
				this.file.seek(8);
				this.file.writeLong(paginaFilho);

			}

			// Senão, faz a fusão com o irmão direito, assumindo que ele existe
			else {
				// Se a página reduzida não for folha, então o elemento
				// do pai deve descer para o irmão
				if (pFilho.filhos.get(0) != -1) {
					pFilho.elementos.add(pa.elementos.remove(diminuido));
					pFilho.filhos.add(pIrmaoDir.filhos.remove(0));
				}
				// Senão, apenas remove o elemento do pai
				else {
					pa.elementos.remove(diminuido);
					pFilho.filhos.remove(0);
				}
				pa.filhos.remove(diminuido + 1); // remove o ponteiro para o irmão direito

				// Move todos os registros do irmão da direita
				pFilho.elementos.addAll(pIrmaoDir.elementos);
				pFilho.filhos.addAll(pIrmaoDir.filhos);
				pIrmaoDir.elementos.clear();
				pIrmaoDir.filhos.clear();

				// Se a página for folha, copia o ponteiro para a próxima página
				pFilho.proxima = pIrmaoDir.proxima;

				// Insere o irmão da direita na lista de páginas excluídas
				this.file.seek(8);
				pIrmaoDir.proxima = this.file.readLong();
				this.file.seek(8);
				this.file.writeLong(paginaIrmaoDir);

			}

			// testa se o pai também ficou sem o número mínimo de elementos
			diminuiu = pa.elementos.size() < maxElementos / 2;

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
	public void build(Database database) throws FileNotFoundException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'build'");
	}

	@Override
	public void clear() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'clear'");
	}
}

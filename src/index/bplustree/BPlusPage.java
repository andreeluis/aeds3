package index.bplustree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BPlusPage {
	public int order; // Número máximo de filhos que uma página pode ter
	public int maxElementos; // Variável igual a ordem - 1 para facilitar a clareza do código
	public int maxFilhos; // Variável igual a ordem para facilitar a clareza do código
	public int TAMANHO_ELEMENTO; // Os elementos são de tamanho fixo
	public int TAMANHO_PAGINA; // A página será de tamanho fixo, calculado a partir da ordem

	public ArrayList<BPlusRegister> elementos; // Elementos da página
	public ArrayList<Long> filhos; // Vetor de ponteiros para os filhos
	public long proxima; // Próxima folha, quando a página for uma folha

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


	// constructors
	public BPlusPage(int order) throws IOException {
		this.setOrder(order);

		this.maxFilhos = this.order;
		this.maxElementos = this.order - 1;
		this.elementos = new ArrayList<>(this.maxElementos);
		this.filhos = new ArrayList<>(this.maxFilhos);
		this.proxima = -1;

		// Cálculo do tamanho (fixo) da página
		// cada elemento -> depende do objeto
		// cada ponteiro de filho -> 8 bytes
		// último filho -> 8 bytes
		// ponteiro próximo -> 8 bytes
		this.TAMANHO_ELEMENTO = BPlusRegister.getSize();
		this.TAMANHO_PAGINA = 4 + this.maxElementos * this.TAMANHO_ELEMENTO + this.maxFilhos * 8 + 8;
	}

	// Retorna o vetor de bytes que representa a página para armazenamento em
	// arquivo
	public byte[] toByteArray() throws IOException {

		// Um fluxo de bytes é usado para construção do vetor de bytes
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(ba);

		// Quantidade de elementos presentes na página
		out.writeInt(this.elementos.size());

		// Escreve todos os elementos
		int i = 0;
		while (i < this.elementos.size()) {
			out.writeLong(this.filhos.get(i).longValue());
			out.write(this.elementos.get(i).toByteArray());
			i++;
		}
		if (this.filhos.size() > 0)
			out.writeLong(this.filhos.get(i).longValue());
		else
			out.writeLong(-1L);

		// Completa o restante da página com registros vazios
		byte[] registroVazio = new byte[TAMANHO_ELEMENTO];
		while (i < this.maxElementos) {
			out.write(registroVazio);
			out.writeLong(-1L);
			i++;
		}

		// Escreve o ponteiro para a próxima página
		out.writeLong(this.proxima);

		// Retorna o vetor de bytes que representa a página
		return ba.toByteArray();
	}

	// Reconstrói uma página a partir de um vetor de bytes lido no arquivo
	public void fromByteArray(byte[] buffer) throws IOException {

		// Usa um fluxo de bytes para leitura dos atributos
		ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
		DataInputStream in = new DataInputStream(ba);

		// Lê a quantidade de elementos da página
		int n = in.readInt();

		// Lê todos os elementos (reais ou vazios)
		int i = 0;
		this.elementos = new ArrayList<>(this.maxElementos);
		this.filhos = new ArrayList<>(this.maxFilhos);
		BPlusRegister elem;
		while (i < n) {
			this.filhos.add(in.readLong());

			byte[] registro = new byte[TAMANHO_ELEMENTO];
			in.read(registro);
			elem = new BPlusRegister(registro);

			this.elementos.add(elem);
			i++;
		}
		this.filhos.add(in.readLong());
		in.skipBytes((this.maxElementos - i) * (TAMANHO_ELEMENTO + 8));
		this.proxima = in.readLong();
	}
}

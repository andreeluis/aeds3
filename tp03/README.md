# TP 03

## Orientações
Neste trabalho, você deverá implementar Compressão de Dados e Reconhecimento de Padrões dentro do contexto do seu [TP01](../tp01/) e [TP02](../tp02/).

### Compressão de Dados
Você deve implementar dois algoritmos de compressão de dados: **Huffman** e **LZW**.

No seu menu de opções apresentado ao usuário, ofereça a possibilidade dele escolher uma opção para realizar a compressão na base de dados criada e crie uma opção para ele realizar a descompressão de alguma versão de compressão criada.

Quando o usuário escolher a opção de compressão, a base de dados deve passar pela compressão usando os dois algoritmos e os novos arquivos gerados devem seguir o nome: “nomeArquivoNomeAlgoritmoCompressaoX”, em que X representa a versão da compressão, nomeArquivo o nome original do arquivo e nomeAlgoritmo o nome do algoritmo usado.

Além de realizar a compressão e gerar os novos arquivos, o algoritmo deve mostrar para o usuário a porcentagem de ganho ou perda de cada algoritmo e o tempo de execução de cada, comparando as execuções e mostrando qual algoritmo (Huffman ou LZW) foi melhor para aquela condição.

A compressão deve ser feita em todos os campos do arquivo, incluindo o cabeçalho, indicadores de tamanho de strings e afins.

Caso o usuário escolha descompactar o arquivo, ele deve passar a versão “X” que deseja, e a descompressão deve substituir o arquivo de dados pelo arquivo gerado pela descompressão. Novamente, o algoritmo deve mostrar para o usuário o tempo de execução de cada, comparando as execuções e mostrando qual algoritmo (Huffman ou LZW) foi melhor para aquela condição de descompactação.
Para o algoritmo de LZW, você é responsável pela definição do dicionário inicial.
As decisões relacionadas ao algoritmo são de responsabilidade do grupo.


### Reconhecimento de Padrões
Você deve implementar dois algoritmos de reconhecimento de padrões. A escolha dos algoritmos é livre, exceto pelo algoritmo de Força-Bruta.

Adicione ao seu TP uma busca de determinado padrão em todo o arquivo ou em algum campo que escolher.
A escolha dos algoritmos utilizados deve ser justificada na parte escrita.

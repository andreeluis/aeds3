# Trabalhos Práticos
Este repositório contém os trabalhos práticos que envolvem a manipulação e otimização de bases de dados, com foco em operações **CRUD** (Create, Read, Update, Delete), indexação, compressão de dados, reconhecimento de padrões e criptografia. Os seguintes trabalhos são realizados utilizando uma base de dados específica, escolhida no início do projeto.

Para os trabalhos práticos, deverá ser escolhida uma base de dados que deve conter ao menos um campo de cada um dos seguintes tipos:
- Tamanho fixo
- Tamanho variável
- Data
- Lista de valores com separador definido

## Trabalho Prático 1
No Trabalho Prático 1, o objetivo é criar um sistema de gerenciamento de dados em um arquivo binário, com operações de CRUD sequenciais. A estrutura dos dados será definida com base nos seguintes requisitos:

- **Cabeçalho:** O arquivo deve começar com um campo int para armazenar o último valor de ID utilizado.
- **Registros:** Cada registro será composto por:
	- **Lápide:** Um byte indicando se o registro é válido ou foi excluído.
	- **Indicador de Tamanho do Registro:** Um número inteiro indicando o tamanho do vetor de bytes do registro.
	- **Vetor de Bytes:** O vetor de bytes que descreve os dados do objeto.

### Funcionalidades do CRUD
- **Carga de Dados:** O sistema deve permitir a carga de dados de um arquivo no formato `.csv`, ou outro formato pertinente, para um arquivo binário.
- **Inserção de Registro:** O sistema deve permitir a inserção de um novo registro no arquivo.
- **Leitura de Registro:** O sistema deve ser capaz de ler um registro a partir de seu id.
- **Atualização de Registro:** O sistema deve permitir a atualização de um registro, tratando casos onde o tamanho do registro possa variar.
- **Exclusão de Registro:** O sistema deve permitir a exclusão de um registro por meio da marcação de lápide.
- **Ordenação Externa:**
	O sistema deve oferecer uma funcionalidade para realizar a ordenação externa dos dados, especificando:
	- O número de caminhos.
	- O número máximo de registros a serem ordenados em memória.


## Trabalho Prático 2
O Trabalho Prático 2 foca na implementação de técnicas de indexação para otimizar as operações de busca no arquivo de dados.

### Estruturas de Indexação
1. **Árvore B, B+ ou B*:**
	- Escolha de uma árvore de índice (**B**, **B+** ou **B***), com justificativa para a escolha.
	- A árvore deverá ter ordem configurável e o arquivo de índices deverá conter o `id` e a posição do registro no arquivo de dados.
	- O arquivo de índice será atualizado automaticamente sempre que houver alterações no arquivo de dados.

2. **Hashing Estendido:**
	- A função de *hash* utilizada será $h(k) = k \mod 2^p$, onde $p$ é a profundidade do diretório.
	- O tamanho dos *buckets* poderá ser configurado.
	- O arquivo de índices será atualizado conforme alterações nos dados.

### Lista Invertida
Criação de dois arquivos de listas invertidas, aplicados aos campos do arquivo de dados.
- As listas invertidas devem ser atualizadas em tempo real sempre que registros forem inseridos, alterados ou excluídos.
- O sistema deve ser capaz de realizar buscas utilizando essas listas invertidas, incluindo a combinação das duas listas em uma única pesquisa.

### CRUD com Índices
O sistema deve realizar operações de CRUD utilizando as estruturas de índices (**Árvore B**, **Hashing Estendido** ou **Lista Invertida**), com a indicação de qual estrutura foi utilizada para cada operação.


## Trabalho Prático 3
O Trabalho Prático 3 visa implementar técnicas de Compressão de Dados e Reconhecimento de Padrões.

### Compressão de Dados
Implementação de dois algoritmos de compressão: **Huffman** e **LZW**:
- O sistema deve permitir a compressão e descompressão da base de dados utilizando ambos os algoritmos, gerando arquivos comprimidos com o formato `nomeArquivo.nomeAlgoritmo.extensaoArquivo`.
- O algoritmo deve calcular e mostrar a porcentagem de ganho ou perda de cada algoritmo, além do tempo de execução.
- O algoritmo de **LZW** deve ter um dicionário inicial definido.

### Reconhecimento de Padrões
Implementação de dois algoritmos de reconhecimento de padrões, com a escolha sendo livre, exceto pelo uso do algoritmo de Força Bruta.
- O sistema deve permitir a busca de padrões em todo o arquivo ou em campos específicos, com a justificativa da escolha dos algoritmos utilizados.


## Trabalho Prático 4
No Trabalho Prático 4, o objetivo é implementar dois algoritmos de criptografia no contexto da base de dados criada nos trabalhos anteriores, garantindo a segurança dos dados armazenados.

### Algoritmo de Criptografia Simples:
Você deve escolher e implementar um dos seguintes algoritmos de criptografia simples:
- **Substituição:** Um algoritmo de substituição simples, onde cada caractere do texto é substituído por outro.
- **Vigenère:** Um método de cifra que usa uma chave para realizar substituições com base em um alfabeto de tamanho fixo.
- **Transposição (Colunas):** Um algoritmo de transposição onde as letras de uma mensagem são rearranjadas com base em um padrão de colunas.

### Algoritmo de Criptografia Moderna:
Você deve implementar um algoritmo de criptografia moderna entre as opções a seguir:
- **DES (Data Encryption Standard):** Um algoritmo de criptografia simétrica que utiliza uma chave de 56 bits para criptografar dados em blocos de 64 bits.
- **RSA (Rivest–Shamir–Adleman):** Um algoritmo de criptografia assimétrica que utiliza duas chaves, uma pública para criptografar e uma privada para descriptografar.

### Implementação no Banco de Dados
O sistema deve permitir que os dados de pelo menos um campo ou de todo o registro no banco de dados sejam criptografados.
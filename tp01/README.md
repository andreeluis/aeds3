# TP 01

## Orientações
- O sistema deve ser implementado em Java. Todo o código deve ser de autoria do grupo (com exceção para bibliotecas/classes relacionadas a aberturas e escritas/leituras de arquivos e conversões entre atributos e campos).
- Todo o código deve ser comentado de modo a se compreender a lógica utilizada.
- A estrutura do arquivo, onde as operações de CRUD serão realizadas, deve ser a seguinte:
	- Deve-se utilizar um int no cabeçalho para armazenar o último valor de id utilizado.
	- Os registros do arquivo devem ser compostos por:
		- Lápide - Byte que indica se o registro é válido ou se é um registro excluído;
		- Indicador de tamanho do registro - Número inteiro que indica o tamanho do vetor de bytes;
		- Vetor de bytes - Bytes que descrevem o objeto.
- Os objetos utilizados devem possuir os atributos que representam a entidade da base de dados que você escolheu.

### CRUD Sequencial:
- O sistema deverá oferecer uma tela inicial (com uso pelo terminal) com um menu com as seguintes opções:
	- Realizar a carga da base de dados selecionada, através da importação de arquivo CSV, de rota de API ou outro formato que julgar pertinente, para um arquivo binário.
	- Ler um registro (id): esse método deve receber um id como parâmetro, percorrer o arquivo binário e retornar os dados do id informado.
	- Atualizar um registro: esse método deve receber novas informações sobre um objeto e atualizar os valores dele no arquivo binário. Observe duas possibilidades que podem acontecer:
		- O registro mantém seu tamanho: Nenhum problema aqui. Basta atualizar os dados no próprio local.
		- O registro aumenta ou diminui de tamanho: O registro anterior deve ser apagado (por meio da marcação lápide) e o novo registro deve ser escrito no fim do arquivo.
	- Deletar um registro (id) -> esse método deve receber um id como parâmetro, percorrer o arquivo binário e colocar uma marcação (lápide) no registro que será considerado deletado.

### Ordenação Externa
O sistema deverá oferecer um menu adicional tela inicial (com uso pelo terminal) com a opção de ordenação externa do arquivo de dados, recebendo como parâmetros:
- O número de Caminhos
- O número de Registros máximo a cada ordenação em memória primária

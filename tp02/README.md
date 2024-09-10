# TP 02

## Orientações
Neste trabalho, você deverá manipular de forma indexada a base de dados criada no [TP01](../tp01/).

### Indexação
O arquivo de índices deve usar como chave o campo id.

- **Usando Árvore B, B+ ou B\***
	Você deve identificar e escolher qual árvore será usada (B, B+ ou B*). A escolha deve ser justificada. Independente de qual a escolha feita no item anterior, a árvore deve ter ordem 8.
	O arquivo de índices deve conter o id e a posição do registro (referente a esse id) no arquivo de dados.

	Sempre que acontecerem alterações no arquivo de dados, novas alterações devem ser feitas no arquivo de índices, mantendo sempre a coerência entre esses arquivos.
	O arquivo de índice criado deve possibilitar a realização de buscas no arquivo de dados.
- **Usando Hashing Estendido**
	Você deve identificar e escolher em seu arquivo o campo que será indexado. Cada escolha deve ser justificada.

	Deve-se usar a função hash $h(𝑘) = 𝑘 \, mod \, 2^p$, em que $p$ é o número de bits (profundidade) usado no diretório, sendo que cada bucket pode armazenar até $x$ registros, sendo $x$ 2% do tamanho inicial de sua base.
	O arquivo de índices deve conter o id e a posição do registro (referente a esse id) no arquivo de dados.

	Sempre que acontecerem alterações no arquivo de dados, novas alterações devem ser feitas no arquivo de índices, mantendo sempre a coerência entre esses arquivos.
	Deve existir a possibilidade de realizar buscas usando a estrutura de índices de Hashing Estendido.

### Lista Invertida
Deve-se criar dois arquivos contendo listas invertidas.
Você deve identificar e escolher em seu arquivo como as listas invertidas serão aplicadas.

O sistema deverá realizar alterações nas listas invertidas sempre que novos registros forem inseridos, alterados ou deletados no arquivo de dados.
O sistema deve ser capaz de receber uma busca usando as listas invertidas criadas. Inclusive, deve ser possível utilizar as duas listas invertidas em uma mesma pesquisa
 
## CRUD com índices
Realize as operações de CRUD agora com apoio dos índices.
A cada operação de CRUD você deverá indicar se a operação será feita utilizando Árvore B, Hash ou a Lista Invertida.

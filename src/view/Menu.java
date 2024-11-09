package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import controller.AppController;
import model.CompressionStats;
import model.Register;
import model.interfaces.Compression;
import model.interfaces.MenuFactory;
import util.ConfigUtil;
import util.FileUtil;
import util.ParseUtil;

public class Menu<T extends Register> {
	private static Scanner scanner = new Scanner(System.in);
	private AppController<T> controler;
	private MenuFactory<T> menuFactory;
	private String entityName;

	public Menu(AppController<T> controler, MenuFactory<T> menuFactory) {
		this.controler = controler;
		this.menuFactory = menuFactory;
		this.entityName = menuFactory.getEntityName();

		menu();
	}

	private void showMenu() {
		clearTerminal();

		System.out.println("Gerenciador de " + this.entityName + "s");
		System.out.println("  1 - Carregar " + this.entityName.toLowerCase() + "s de um .csv");
		System.out.println("  2 - Adicionar novo(a) " + this.entityName.toLowerCase());
		System.out.println("  3 - Buscar por " + this.entityName.toLowerCase() + "s");
		System.out.println("  4 - Atualizar um(a) " + this.entityName.toLowerCase());
		System.out.println("  5 - Excluir um(a) " + this.entityName.toLowerCase());
		System.out.println("  6 - Ordenar (e limpar) registros");
		System.out.println("  7 - Configurar índices");
		System.out.println("  8 - Comprimir arquivo");
		System.out.println("  9 - Descomprimir arquivo");

		System.out.println("  0 - Sair");

		System.out.print("Selecione a opção desejada: ");
	}

	private void menu() {
		int option;

		do {
			showMenu();
			option = scanner.nextInt();
			scanner.nextLine(); // limpa o buffer

			clearTerminal();

			switch (option) {
				case 1:
					readFromCSV();
					break;
				case 2:
					createRegister();
					break;
				case 3:
					searchRegister();
					break;
				case 4:
					editRegister();
					break;
				case 5:
					deleteRegister();
					break;
				case 6:
					sortRegisters();
					break;
				case 7:
					index();
					break;
				case 8:
					compress();
					break;
				case 9:
					decompress();
					break;
				case 0:
					return;
				default:
					break;
			}

			scanner.nextLine(); // Espera um enter
		} while (option != 0);
	}

	private void readFromCSV() {
		System.out.print("Qual caminho do arquivo a ser lido? ");
		String path = scanner.nextLine();
		path = path.isBlank() ? "../dataset/movies.csv" : path;

		controler.readFromCSV(path);
		System.out.println("Registros carregados com sucesso!");
	}

	private void createRegister() {
		T register = menuFactory.createRegister(scanner);
		controler.insertRegister(register);
	}

	private void searchRegister() {
		int option = menuFactory.searchRegister(scanner);
		scanner.nextLine(); // Limpa o buffer

		List<T> registers = new ArrayList<>();
		String title, description;
		switch (option) {
			case 1:
				System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + "? ");
				int id = scanner.nextInt();
				scanner.nextLine(); // Limpa o buffer

				controler.searchById(id).ifPresent(registers::add);

				break;
			case 2:
				System.out.print("Qual o título do(a) " + entityName.toLowerCase() + "? ");
				title = scanner.nextLine();

				controler.searchByFields(new String[] { "Title" }, new String[] { title }).ifPresent(registers::addAll);

				break;
			case 3:
				System.out.print("Qual a descrição do(a) " + entityName.toLowerCase() + "? ");
				description = scanner.nextLine();

				controler.searchByFields(new String[] { "Description" }, new String[] { description })
						.ifPresent(registers::addAll);

				break;
			case 4:
				System.out.print("Qual o título do(a) " + entityName.toLowerCase() + "? ");
				title = scanner.nextLine();

				System.out.print("Qual a descrição do(a) " + entityName.toLowerCase() + "? ");
				description = scanner.nextLine();

				controler.searchByFields(new String[] { "title", "description" }, new String[] { title, description })
						.ifPresent(registers::addAll);

				break;
			default:
				break;
		}

		if (!registers.isEmpty()) {
			System.out.println(entityName + "s encontrados(as):");
			for (T register : registers) {
				System.out.println(register);
			}
		} else {
			System.out.println("Nenhum(a) " + entityName.toLowerCase() + " foi encontrado(a).");
		}
	}

	private void editRegister() {
		System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + " a ser alterado(a)? ");
		int id = scanner.nextInt();
		scanner.nextLine(); // Limpa o buffer

		Optional<T> register = controler.searchById(id);

		if (register.isPresent()) {
			System.out.println("Editando " + entityName.toLowerCase() + " " + register.get() + ":");

			T newRegister = menuFactory.editRegister(scanner, register.get());
			controler.updateRegister(id, newRegister);

			System.out.println(entityName + " " + newRegister + " atualizado(a) com sucesso!");
		} else {
			System.out.println(entityName + " não encontrado(a) para alteração.");
		}
	}

	private void deleteRegister() {
		System.out.print("Qual o ID do(a) " + entityName.toLowerCase() + " a ser excluído(a)? ");
		int id = scanner.nextInt();
		scanner.nextLine(); // Limpa o buffer

		Optional<T> register = controler.deleteRegister(id);

		if (register.isPresent()) {
			System.out.println(entityName + register.get() + " excluído(a) com sucesso!");
		} else {
			System.out.println(entityName + " não encontrado(a) para exclusão.");
		}
	}

	private void sortRegisters() {
		System.out.print("Quantos caminhos (arquivos temporários) para a ordenação?");
		int pathsNumber = ParseUtil.parseInt(scanner.nextLine());

		System.out.print("Quantos registros em memória primária para a ordenação? ");
		int inMemoryRegisters = ParseUtil.parseInt(scanner.nextLine());

		if (controler.sort(pathsNumber, inMemoryRegisters)) {
			System.out.println("Registros ordenados com sucesso!");
		} else {
			System.out.println("Os registros não foram ordenados.");
		}
	}

	private void index() {
		System.out.println("Qual índice deseja configurar?");
		System.out.println("  1 - Definir índice padrão");
		System.out.println("  2 - Árvore B+");
		System.out.println("  3 - Hash Extensível");
		System.out.print("Selecione a opção desejada: ");
		int op = scanner.nextInt();
		scanner.nextLine(); // Limpa o buffer

		switch (op) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			default:
				break;
		}
	}

	private void compress() {
		// List of all files
		Optional<List<String>> files = FileUtil.getAllFiles();

		// early return
		if (files.isEmpty()) {
			System.out.println("Nenhum arquivo encontrado para compressão.");
			return;
		}

		System.out.println("Qual arquivo deseja comprimir?");
		for (int i = 0; i < files.get().size(); i++) {
			System.out.println("  " + (i + 1) + " - " + files.get().get(i));
		}

		System.out.print("Selecione a opção desejada: ");
		int select = scanner.nextInt();
		scanner.nextLine(); // Limpa o buffer

		String fileName = files.get().get(select - 1);

		// Compress file
		Optional<List<CompressionStats>> stats = controler.compressFile(ConfigUtil.DB_PATH + fileName);

		if (stats.isPresent()) {
			System.out.println("Arquivo comprimido com sucesso!");
			System.out.println("Estatísticas da compressão:");
			for (CompressionStats stat : stats.get()) {
				System.out.println("  - " + stat);
			}
		} else {
			System.out.println("Erro ao comprimir o arquivo.");
		}
	}

	private void decompress() {
		// File Extensions
		Optional<List<Compression>> compressions = controler.getAvailableCompressions();

		// early return
		if (compressions.isEmpty()) {
			System.out.println("Nenhum algoritmo de descompressão disponível.");
			return;
		}

		// List of all files
		Optional<List<String>> files = FileUtil.getAllFiles(
			compressions.get().stream().map(Compression::getExtension).collect(Collectors.toList())
		);

		// early return
		if (files.isEmpty()) {
			System.out.println("Nenhum arquivo encontrado para descompressão.");
			return;
		}

		System.out.println("Qual arquivo deseja descomprimir?");
		for (int i = 0; i < files.get().size(); i++) {
			System.out.println("  " + (i + 1) + " - " + files.get().get(i));
		}

		System.out.print("Selecione a opção desejada: ");
		int select = scanner.nextInt();
		scanner.nextLine(); // Limpa o buffer

		String fileName = files.get().get(select - 1);

		// Decompress file
		Optional<CompressionStats> stats = controler.decompressFile(ConfigUtil.DB_PATH + fileName);

		if (stats.isPresent()) {
			System.out.println("Arquivo descomprimido com sucesso!");
			System.out.println("Estatísticas da descompressão:");
			System.out.println("  - " + stats.get());
		} else {
			System.out.println("Erro ao descomprimir o arquivo.");
		}
	}

	private static void clearTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}

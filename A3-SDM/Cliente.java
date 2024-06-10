import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Informe o endereço IP do servidor: ");
            String serverAddress = scanner.nextLine().trim();  // O "trim" é para remover espaços em branco
            System.out.println("Informe a porta do servidor: ");
            int serverPort = Integer.parseInt(scanner.nextLine().trim());

            try (Socket socket = new Socket(serverAddress, serverPort);
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); //UTF-8 é para a saída contemplar qualquer caractere universal
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

                String resposta;

                // Recebe o pedido de ID do jogador e envia o ID --> Referência a classe Servidor.java "out.println("Informe seu ID de jogador:");"
                System.out.println(in.readLine());
                String clientId = scanner.nextLine();
                out.println(clientId);

                // Recebe a escolha do tipo de jogo e envia a escolha
                System.out.println(in.readLine()); // "Escolha com quem você quer jogar:"
                System.out.println(in.readLine()); // "1 - Contra o Servidor (CPU)"
                System.out.println(in.readLine()); // "2 - Contra outro Jogador"
                String choice = scanner.nextLine();
                out.println(choice);

                // Espera resposta do servidor
                while ((resposta = in.readLine()) != null) {
                    System.out.println(resposta);

                    if (resposta.contains("Sua vez")) {
                        String move = scanner.nextLine();
                        out.println(move);
                    }
                }
            } catch (IOException e) {
            }
        } catch (NumberFormatException e) {

        }
    }
}

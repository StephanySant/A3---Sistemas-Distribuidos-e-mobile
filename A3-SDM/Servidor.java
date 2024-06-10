import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {

    private static int gerarPortaConexao() {
        Random random = new Random();
        return random.nextInt(16384) + 49152;
    }
    private static final int PORT = gerarPortaConexao();
    static ConcurrentHashMap<Integer, GameSession> sessions = new ConcurrentHashMap<>();
    static AtomicInteger sessionIdCounter = new AtomicInteger(0);
    static BlockingQueue<Socket> waitingPlayers = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Servidor iniciado na porta " + PORT + " no endereço IP " + ipAddress);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
        }
    }
}



class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private int gameMode;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            out.println("Informe seu ID de jogador:");
            clientId = in.readLine();

            out.println("Escolha com quem você quer jogar:");
            out.println("1 - Contra o Servidor (CPU)");
            out.println("2 - Contra outro Jogador");
            gameMode = Integer.parseInt(in.readLine());

            if (gameMode == 1) {
                new Thread(new GameSessionCPU(socket, clientId)).start();
            } else {
                Servidor.waitingPlayers.add(socket);
                if (Servidor.waitingPlayers.size() >= 2) {
                    Socket player1 = Servidor.waitingPlayers.poll();
                    Socket player2 = Servidor.waitingPlayers.poll();
                    int sessionId = Servidor.sessionIdCounter.incrementAndGet();
                    GameSession session = new GameSession(sessionId, player1, player2);
                    Servidor.sessions.put(sessionId, session);
                    new Thread(session).start();
                } else {
                    out.println("Aguardando outro jogador para iniciar o jogo...");
                }
            }
        } catch (IOException e) {
        }
    }
}

class GameSession implements Runnable {
    private final int sessionId;
    private final Socket socketJogador1;
    private final Socket socketJogador2;
    private final Game game;

    public GameSession(int sessionId, Socket socketJogador1, Socket socketJogador2) {
        this.sessionId = sessionId;
        this.socketJogador1 = socketJogador1;
        this.socketJogador2 = socketJogador2;
        this.game = new Game();
    }

    @Override
    public void run() {
        try (BufferedReader entradaJogador1 = new BufferedReader(new InputStreamReader(socketJogador1.getInputStream(), "UTF-8"));
             PrintWriter saidaJogador1 = new PrintWriter(new OutputStreamWriter(socketJogador1.getOutputStream(), "UTF-8"), true);
             BufferedReader entradaJogador2 = new BufferedReader(new InputStreamReader(socketJogador2.getInputStream(), "UTF-8"));
             PrintWriter saidaJogador2 = new PrintWriter(new OutputStreamWriter(socketJogador2.getOutputStream(), "UTF-8"), true)) {

            saidaJogador1.println("Conectado à sessão " + sessionId + ". Você é o Jogador 1.");
            saidaJogador2.println("Conectado à sessão " + sessionId + ". Você é o Jogador 2.");

            String move1, move2;

            //Aqui é definido a quantidade de rodadas por partida.
            for (int round = 1; round <= 3; round++) {
                saidaJogador1.println("Rodada " + round + ". Sua vez (PEDRA, PAPEL, TESOURA): ");
                move1 = entradaJogador1.readLine();
                while (move1 == null || !game.movimentoValido(move1)) {
                    saidaJogador1.println("Movimento inválido! Tente novamente (PEDRA, PAPEL, TESOURA): ");
                    move1 = entradaJogador1.readLine();
                }

                saidaJogador2.println("Rodada " + round + ". Aguardando jogada do Jogador 1...");

                saidaJogador2.println("Rodada " + round + ". Sua vez (PEDRA, PAPEL, TESOURA): ");
                move2 = entradaJogador2.readLine();
                while (move2 == null || !game.movimentoValido(move2)) {
                    saidaJogador2.println("Movimento inválido! Tente novamente (PEDRA, PAPEL, TESOURA): ");
                    move2 = entradaJogador2.readLine();
                }

                String result = game.processoDeEntrada(move1, move2);
                saidaJogador1.println("Jogada do oponente: " + move2 + ". " + result);
                saidaJogador2.println("Jogada do oponente: " + move1 + ". " + result);

                saidaJogador1.println("Fim da rodada " + round);
                saidaJogador2.println("Fim da rodada " + round);
            }

            saidaJogador1.println("Resultado final:");
            saidaJogador1.println("Vitórias Jogador 1: " + game.getJogador1());
            saidaJogador1.println("Vitórias Jogador 2: " + game.getJogador2());
            saidaJogador1.println("Empates: " + game.getTies());
            saidaJogador1.println("Fim do jogo");

            saidaJogador2.println("Resultado final:");
            saidaJogador2.println("Vitórias Jogador 1: " + game.getJogador1());
            saidaJogador2.println("Vitórias Jogador 2: " + game.getJogador2());
            saidaJogador2.println("Empates: " + game.getTies());
            saidaJogador2.println("Fim do jogo");

        } catch (IOException e) {
        }
    }

    public Game getGame() {
        return game;
    }
}

class GameSessionCPU implements Runnable {
    private final Socket playerSocket;
    private final Game game;

    public GameSessionCPU(Socket playerSocket, String clientId) {
        this.playerSocket = playerSocket;
        this.game = new Game();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(playerSocket.getOutputStream(), "UTF-8"), true)) {

            out.println("Conectado à sessão contra o CPU. Você é o Jogador 1.");

            String move1, move2;
            Random random = new Random();

            for (int round = 1; round <= 3; round++) {
                out.println("Rodada " + round + ". Sua vez (PEDRA, PAPEL, TESOURA): ");
                move1 = in.readLine();
                while (move1 == null || !game.movimentoValido(move1)) {
                    out.println("Movimento inválido! Tente novamente (PEDRA, PAPEL, TESOURA): ");
                    move1 = in.readLine();
                }

                String[] moves = {"PEDRA", "PAPEL", "TESOURA"};
                move2 = moves[random.nextInt(moves.length)];

                out.println("Jogada do CPU: " + move2);

                String result = game.processoDeEntrada(move1, move2);
                out.println(result);
                out.println("Fim da rodada " + round);
            }

            out.println("Resultado final:");
            out.println("Vitórias Jogador: " + game.getJogador1());
            out.println("Vitórias CPU: " + game.getJogador2());
            out.println("Empates: " + game.getTies());
            out.println("Fim do jogo");

        } catch (IOException e) {
        }
    }

    public Game getGame() {
        return game;
    }
}

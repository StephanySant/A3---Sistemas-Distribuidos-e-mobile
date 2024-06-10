public class Game {
    private int jogador1;
    private int jogador2;
    private int ties;

    public Game() {
        this.jogador1 = 0;
        this.jogador2 = 0;
        this.ties = 0;
    }

    public boolean movimentoValido(String move) {
        return "PEDRA".equalsIgnoreCase(move) || "PAPEL".equalsIgnoreCase(move) || "TESOURA".equalsIgnoreCase(move);
    }

    public String processoDeEntrada(String movimentoJogador1, String movimentoJogador2) {
        String result = determinarVencedor(movimentoJogador1, movimentoJogador2);
        return result + " | Vitórias Jogador 1: " + jogador1 + ", Vitórias Jogador 2: " + jogador2 + ", Empates: "
                + ties;
    }

    private String determinarVencedor(String movimentoJogador1, String movimentoJogador2) {
        if (movimentoJogador1.equalsIgnoreCase(movimentoJogador2)) {
            ties++;
            return "Empate! Ambos escolheram " + movimentoJogador1;
        }

        switch (movimentoJogador1.toLowerCase()) {
            case "pedra":
                if (movimentoJogador2.equalsIgnoreCase("tesoura")) {
                    jogador1++;
                    return "Jogador 1 venceu! Pedra quebra tesoura.";
                } else {
                    jogador2++;
                    return "Jogador 2 venceu! Papel embrulha pedra.";
                }
            case "papel":
                if (movimentoJogador2.equalsIgnoreCase("pedra")) {
                    jogador1++;
                    return "Jogador 1 venceu! Papel embrulha pedra.";
                } else {
                    jogador2++;
                    return "Jogador 2 venceu! Tesoura corta papel.";
                }
            case "tesoura":
                if (movimentoJogador2.equalsIgnoreCase("papel")) {
                    jogador1++;
                    return "Jogador 1 venceu! Tesoura corta papel.";
                } else {
                    jogador2++;
                    return "Jogador 2 venceu! Pedra quebra tesoura.";
                }
            default:
                return "Movimento inválido!";
        }
    }

    public int getJogador1() {
        return jogador1;
    }

    public int getJogador2() {
        return jogador2;
    }

    public int getTies() {
        return ties;
    }
}

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class Grafo {

    // Classe interna para representar uma aresta com peso
    static class ArestaPonderada {
        int origem;
        int destino;
        double peso;

        ArestaPonderada(int origem, int destino, double peso) {
            this.origem = origem;
            this.destino = destino;
            this.peso = peso;
        }
    }

    public static List<int[]> lerArestas(String nomeArquivo) throws IOException {
        List<int[]> arestas = new ArrayList<>();
        InputStream is = Grafo.class.getClassLoader().getResourceAsStream(nomeArquivo);
        if (is == null) {
            throw new IOException("Arquivo não encontrado no classpath: " + nomeArquivo);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.trim().split("\\s+");
                if (partes.length >= 2) {
                    int u = Integer.parseInt(partes[0]);
                    int v = Integer.parseInt(partes[1]);
                    arestas.add(new int[]{u, v});
                }
            }
        }
        return arestas;
    }

    // --- Questão 3: Construir grafo invertido (transposto) ---
    public static Map<Integer, List<Integer>> grafoInvertido(List<int[]> arestas) {
        Map<Integer, List<Integer>> invertido = new HashMap<>();
        for (int[] aresta : arestas) {
            int u = aresta[0];
            int v = aresta[1];
            invertido.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
        }
        return invertido;
    }

    // --- Questão 5: BFS no grafo invertido a partir de um vértice ---
    public static int bfsInvertido(Map<Integer, List<Integer>> grafoInv, int inicio) {
        Set<Integer> visitados = new HashSet<>();
        Queue<Integer> fila = new LinkedList<>();

        fila.add(inicio);
        visitados.add(inicio);

        while (!fila.isEmpty()) {
            int atual = fila.poll();
            if (grafoInv.containsKey(atual)) {
                for (int viz : grafoInv.get(atual)) {
                    if (!visitados.contains(viz)) {
                        visitados.add(viz);
                        fila.add(viz);
                    }
                }
            }
        }
        return visitados.size() - 1; // Ignora o próprio nó inicial
    }

    // --- Questão 1: Encontrar os 2 alunos mais alcançáveis ---
    public static List<int[]> maisAlcancados(Map<Integer, List<Integer>> gInv, int total) {
        List<int[]> alcances = new ArrayList<>();
        for (int aluno = 1; aluno <= total; aluno++) {
            int count = bfsInvertido(gInv, aluno);
            alcances.add(new int[]{aluno, count});
        }
        
        alcances.sort((a, b) -> Integer.compare(b[1], a[1]));
        return alcances.subList(0, Math.min(2, alcances.size()));
    }
    
    // --- Questão 6: Fecho transitivo usando algoritmo de Warshall ---
    public static int[][] warshall(int n, List<int[]> arestas) {
        int[][] matriz = new int[n][n];
        for (int[] aresta : arestas) {
            matriz[aresta[0] - 1][aresta[1] - 1] = 1;
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matriz[i][j] = matriz[i][j] | (matriz[i][k] & matriz[k][j]);
                }
            }
        }
        return matriz;
    }

    // --- Questão 13: Com pesos - leitura de arestas ponderadas ---
    public static List<ArestaPonderada> lerArestasPonderadas(String nomeArquivo) throws IOException {
        List<ArestaPonderada> arestas = new ArrayList<>();
        InputStream is = Grafo.class.getClassLoader().getResourceAsStream(nomeArquivo);
        if (is == null) {
            throw new IOException("Arquivo não encontrado no classpath: " + nomeArquivo);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.trim().split("\\s+");
                if (partes.length == 3) {
                    int u = Integer.parseInt(partes[0]);
                    int v = Integer.parseInt(partes[1]);
                    double peso = Double.parseDouble(partes[2]);
                    arestas.add(new ArestaPonderada(u, v, peso));
                }
            }
        }
        return arestas;
    }

    // --- Questão 13: Construir grafo ponderado ---
    public static Map<Integer, List<ArestaPonderada>> construirGrafoPonderado(List<ArestaPonderada> arestasPonderadas) {
        Map<Integer, List<ArestaPonderada>> grafo = new HashMap<>();
        for (ArestaPonderada aresta : arestasPonderadas) {
            grafo.computeIfAbsent(aresta.origem, k -> new ArrayList<>()).add(aresta);
        }
        return grafo;
    }

    // --- Questão 13: Dijkstra para dependência ponderada ---
    public static Map<Integer, Double> dijkstra(Map<Integer, List<ArestaPonderada>> grafo, int origem) {
        Map<Integer, Double> distancias = new HashMap<>();
        PriorityQueue<Map.Entry<Integer, Double>> heap = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        
        // Inicializa todas as distâncias como infinito
        for (int vertice : grafo.keySet()) {
            distancias.put(vertice, Double.POSITIVE_INFINITY);
        }
        distancias.put(origem, 0.0);
        heap.add(Map.entry(origem, 0.0));

        while (!heap.isEmpty()) {
            Map.Entry<Integer, Double> entryAtual = heap.poll();
            int u = entryAtual.getKey();
            double custoAtual = entryAtual.getValue();

            if (custoAtual > distancias.get(u)) {
                continue;
            }

            for (ArestaPonderada aresta : grafo.getOrDefault(u, Collections.emptyList())) {
                double novoCusto = custoAtual + aresta.peso;
                if (novoCusto < distancias.getOrDefault(aresta.destino, Double.POSITIVE_INFINITY)) {
                    distancias.put(aresta.destino, novoCusto);
                    heap.add(Map.entry(aresta.destino, novoCusto));
                }
            }
        }
        return distancias;
    }

    public static void main(String[] args) {
        try {
            // A leitura agora é feita a partir de um arquivo no classpath
            String nomeArquivo = "arquivo_pesos.txt";

            List<int[]> arestas = lerArestas(nomeArquivo);
            Map<Integer, List<Integer>> gInv = grafoInvertido(arestas);

            System.out.println("=== Questão 1: Dois alunos com melhor desempenho ===");
            List<int[]> top2 = maisAlcancados(gInv, 29);
            for (int[] par : top2) {
                System.out.println("Aluno " + par[0] + " é alcançado por " + par[1] + " outros alunos");
            }
            
            System.out.println("\n=== Questão 5: BFS no grafo invertido ===");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Digite um vertice: ");
            int teste = scanner.nextInt();
            System.out.println("Número de alunos que alcançam o aluno " + teste + ": " + bfsInvertido(gInv, teste));
            scanner.close();

            System.out.println("\n=== Questão 6: Fecho Transitivo (Warshall) ===");
            int[][] matriz = warshall(29, arestas);
            System.out.println("Trecho da matriz (linha 0):");
            System.out.println(Arrays.toString(matriz[0]));

            System.out.println("\n=== Questão 13: Dijkstra com pesos ===");
            List<ArestaPonderada> arestasPonderadas = lerArestasPonderadas(nomeArquivo);
            Map<Integer, List<ArestaPonderada>> grafoPeso = construirGrafoPonderado(arestasPonderadas);
            int origem = 1;
            Map<Integer, Double> distancias = dijkstra(grafoPeso, origem);
            System.out.println("Distâncias mínimas a partir do aluno " + origem + ":");
            for (Map.Entry<Integer, Double> entry : distancias.entrySet()) {
                System.out.println(origem + " -> " + entry.getKey() + " = " + entry.getValue());
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
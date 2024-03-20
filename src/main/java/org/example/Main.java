package org.example;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class Main
{
    Gson gson = new Gson();
    HashMap<Long, ArrayList<Long>> nodeAssignment;
    HashMap<Long, Node> idToNode;
    ArrayList<ArrayList<Node>> clusters;
    MapJson map;
    ArrayList<Node> nodes = new ArrayList<>();

    public static void main(String... args)
    {
        Main m = new Main();
        Pair<ArrayList<Pair<Double, Double>>, Double> path = m.getPath(new Pair<>(50.9272391, 5.6877452), new Pair<>(50.7760463, 5.7882736));
    }
    public Main()
    {
        try (Reader reader = new FileReader("MapNodes.json")) {
            map = gson.fromJson(reader, MapJson.class);
            idToNode = new HashMap<>();
            clusters = new ArrayList<>();
            nodeAssignment = new HashMap<>();
            for (Cluster cluster : map.elements) {
                Node[] nodeArr = new Node[cluster.nodes.size()];
                ArrayList<Node> nodesInCluster = new ArrayList<>();

                for (int i = 0; i < cluster.nodes.size(); i++) {
                    Long locationId = cluster.nodes.get(i);
                    Node n;
                    if (!idToNode.containsKey(locationId)) {
                        n = new Node(locationId, new ArrayList<>(), new Geometry());
                        nodes.add(n);
                        idToNode.put(locationId, n);
                    } else {
                        n = idToNode.get(locationId);
                    }
                    nodeArr[i] = n;
                    nodesInCluster.add(n);

                    nodeAssignment.putIfAbsent(n.id, new ArrayList<>());
                    nodeAssignment.get(n.id).add(cluster.id);
                }
                for (int i = 0; i < cluster.geometry.size(); i++) {
                    nodeArr[i].location = cluster.geometry.get(i);
                }
                clusters.add(nodesInCluster);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size(); j++) {
                //if (nodeAssignment.get(clusters.get(i).get(j).id).size() == 1) continue;
                ArrayList<Pair<Node, Double>> adj = new ArrayList<>();
                for (int k = 0; k < clusters.get(i).size(); k++) {
                    if (clusters.get(i).get(j) == clusters.get(i).get(k)) continue;
                    Double distance = 6378160 * 2 * Math.asin(
                            Math.sqrt(
                                    Math.pow(Math.sin(Math.toRadians((clusters.get(i).get(j).location.lat - clusters.get(i).get(k).location.lat) / 2)), 2) +
                                    Math.cos(Math.toRadians(clusters.get(i).get(j).location.lat)) * Math.cos(Math.toRadians(clusters.get(i).get(k).location.lat)) *
                                    Math.pow(Math.sin(Math.toRadians((clusters.get(i).get(j).location.lon - clusters.get(i).get(k).location.lon) / 2)), 2)
                            )
                    );
                    if (distance > 150D) continue;
                    adj.add(new Pair<>(clusters.get(i).get(k), distance));
                }
                clusters.get(i).get(j).adjList.addAll(adj);
            }
        }

        //Pair<ArrayList<Long>, Double> res = dijkstra(nodes.get(0), idToNode.get(5054934211L));
        //Pair<ArrayList<Pair<Double, Double>>, Double> path = getPath(new Pair<>(nodes.get(0).location.lat, nodes.get(0).location.lon), new Pair<>(idToNode.get(5054934211L).location.lat, idToNode.get(5054934211L).location.lon));
    }

    private Map<Long, Long> parentMap = new HashMap<>();
    private Map<Long, Double> distanceMap = new HashMap<>();

    public Pair<ArrayList<Long>, Double> dijkstra(Node source, Node destination)
    {
        PriorityQueue<Pair<Node, Double>> minHeap = new PriorityQueue<>(Comparator.comparingDouble(p -> p.second));
        distanceMap.put(source.id, 0.0);
        minHeap.offer(new Pair<>(source, 0.0));

        while (!minHeap.isEmpty())
        {
            Pair<Node, Double> min = minHeap.poll();
            Node node = min.first;
            double distance = min.second;

            if (node == destination)
            {
                ArrayList<Long> path = new ArrayList<>();
                long current = destination.id;
                while (current != source.id)
                {
                    path.add(current);
                    current = parentMap.get(current);
                }
                path.add(source.id);
                Collections.reverse(path);
                System.out.println("Shortest Path: " + path);
                System.out.println("Shortest Distance: " + distanceMap.get(destination.id));
                return new Pair<>(path, distanceMap.get(destination.id));
            }

            for (Pair<Node, Double> neighborPair : node.adjList)
            {
                Node neighbor = neighborPair.first;
                double weight = neighborPair.second;
                double newDistance = distance + weight;

                if (!distanceMap.containsKey(neighbor.id) || newDistance < distanceMap.get(neighbor.id))
                {
                    distanceMap.put(neighbor.id, newDistance);
                    parentMap.put(neighbor.id, node.id);
                    minHeap.offer(new Pair<>(neighbor, newDistance));
                }
            }
        }
        return new Pair<>();
    }

    public Pair<ArrayList<Pair<Double, Double>>, Double> getPath(Pair<Double, Double> start, Pair<Double, Double> dest)
    {
        Long startId = getClosestNode(start);
        Long destId = getClosestNode(dest);

        Pair<ArrayList<Long>, Double> res = dijkstra(idToNode.get(startId), idToNode.get(destId));
        ArrayList<Pair<Double, Double>> coordinates = new ArrayList<>();
        for (Long id: res.first)
        {
            coordinates.add(new Pair<>(idToNode.get(id).location.lat, idToNode.get(id).location.lon));
        }
        Double distance = res.second + getDistance(start, coordinates.get(0)) + getDistance(dest, coordinates.get(coordinates.size() - 1));
        return new Pair<>(coordinates, distance);
    }

    private Double getDistance(Pair<Double, Double> start, Pair<Double, Double> end)
    {
        Double distance = 6378160 * 2 * Math.asin(
                Math.sqrt(
                        Math.pow(Math.sin(Math.toRadians((start.first - end.first) / 2)), 2) +
                        Math.cos(Math.toRadians(start.first)) * Math.cos(Math.toRadians(end.first)) *
                        Math.pow(Math.sin(Math.toRadians((start.second - end.second) / 2)), 2)
                )
        );
        return distance;
    }

    private Long getClosestNode(Pair<Double, Double> point)
    {
        Node bestNode = clusters.get(0).get(0);
        Double bestValue = Double.MAX_VALUE;

        for (Node node : nodes)
        {
            Double value = Math.abs(point.first - node.location.lat) + Math.abs(point.second - node.location.lon);
            if (value < bestValue)
            {
                bestNode = node;
                bestValue = value;
            }
        }
        return bestNode.id;
    }

//    void dfs(Node node, int it)
//    {
//        node.visited = true;
//        for (Pair<Node, Double> nextPair: node.adjList)
//        {
//            if (!nextPair.first.visited) dfs(nextPair.first, ++it);
//        }
//        System.out.println(node.id + " " + it);
//    }
}
class MapJson
{
    ArrayList<Cluster> elements;
}
class Cluster
{
    String type;
    long id;
    ArrayList<Long> nodes;
    ArrayList<Geometry> geometry;
}
class Geometry
{
    Double lat;
    Double lon;
}
class Node
{
    boolean visited;
    long val;
    long id;
    ArrayList<Pair<Node, Double>> adjList;
    Geometry location;

    public Node(long id, ArrayList<Pair<Node, Double>> adjList, Geometry location)
    {
        this.id = id;
        this.adjList = adjList;
        this.location = location;
        this.visited = false;
    }
}
class Pair<t,p>
{
    t first;
    p second;
    public Pair(t first, p second)
    {
        this.first = first;
        this.second = second;
    }
    public Pair() {}
}
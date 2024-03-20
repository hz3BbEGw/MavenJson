package org.example;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

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
        m.calc();
    }
    public void calc()
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
                if (nodeAssignment.get(clusters.get(i).get(j).id).size() == 1) continue;
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
                    if (distance > 1000D || nodeAssignment.get(clusters.get(i).get(k).id).size() == 1) continue;
                    adj.add(new Pair<>(clusters.get(i).get(k), distance));
                }
                clusters.get(i).get(j).adjList.addAll(adj);
            }
        }

//        for (Pair<Node, Double> n : nodes.get(1).adjList) {
//            System.out.println(n.first.id);
//            System.out.println(n.second);
//        }
//
//        PriorityQueue<Pair<Node, Double>> minHeap = new PriorityQueue<>(Comparator.comparingDouble(p -> p.second));
//        nodes.get(0).adjList.forEach(minHeap::offer);
//
//        while (!minHeap.isEmpty()) {
//            Pair<Node, Double> min = minHeap.poll();
//            Node node = min.first;
//            double distance = min.second;
//            if (node.visited) continue;
//            node.visited = true;
//            //System.out.println("Node: " + node.id + ", Distance: " + distance);
//
//            for (Pair<Node, Double> neighborPair : node.adjList) {
//                Node neighbor = neighborPair.first;
//                double weight = neighborPair.second;
//                if (!neighbor.visited) {
//                    minHeap.offer(new Pair<>(neighbor, distance + weight));
//                }
//            }
//        }
        dfs(clusters.get(0).get(0), 0);
    }
    void dfs(Node node, int it)
    {
        node.visited = true;
        for (Pair<Node, Double> nextPair: node.adjList)
        {
            if (!nextPair.first.visited) dfs(nextPair.first, ++it);
        }
        System.out.println(node.id + " " + it);
    }
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
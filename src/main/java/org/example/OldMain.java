//package org.example;
//
//import com.google.gson.Gson;
//import java.io.File;
//import java.io.FileReader;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Objects;
//
//public class OldMain
//{
//    public static void main(String[] args)
//    {
//        System.out.println("Hello world!");
//        File file = new File("./MapNodes.json");
//        Gson gson = new Gson();
//        HashMap<Long, ArrayList<Long>> nodeAssignment;
//        HashMap<Long, Node> idToNode;
//        MapJson map;
//        ArrayList<Node> nodes = new ArrayList<>();
//
//        try (Reader reader = new FileReader("MapNodes.json")) {
//
//            map = gson.fromJson(reader, MapJson.class);
//            nodeAssignment = new HashMap<>();
//            for (Cluster cluster : map.elements)
//            {
//                for (Long node : cluster.nodes)
//                {
//                    if (!nodeAssignment.containsKey(node)) nodeAssignment.put(node, new ArrayList<>());
//                    nodeAssignment.get(node).add(cluster.id);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            return;
//        }
//        idToNode = new HashMap<>();
//        for (Cluster cluster : map.elements)
//        {
//            double lat = (cluster.bounds.maxlat - cluster.bounds.minlat)/2;
//            double lon = (cluster.bounds.maxlon - cluster.bounds.minlon)/2;
//            Pair<Double, Double> loc = new Pair<>(lat, lon);
//            Node node = new Node(cluster.id, 0, new ArrayList<>(), loc);
//            nodes.add(node);
//            node.val = nodes.size() - 1;
//
//            idToNode.putIfAbsent(cluster.id, node);
//        }
//
//        for (Cluster cluster : map.elements)
//        {
//            for (Long location : cluster.nodes)
//            {
//                for (Long nextLocationId : nodeAssignment.get(location)) {
//                    if (nextLocationId == cluster.id) continue;
//
//                    Node currNode = idToNode.get(cluster.id);
//                    Node nextNode = idToNode.get(nextLocationId);
//
//                    Double distance = 6378160 * 2 * Math.asin(
//                            Math.sqrt(
//                                    Math.pow(Math.sin(Math.toRadians((currNode.location.first - nextNode.location.first) / 2)), 2) +
//                                            Math.cos(Math.toRadians(currNode.location.first)) * Math.cos(Math.toRadians(nextNode.location.first)) *
//                                                    Math.pow(Math.sin(Math.toRadians((currNode.location.second - nextNode.location.second) / 2)), 2)
//                            )
//                    );
//
//                    Pair<Node, Double> pair = new Pair<>(nextNode, distance);
//                    idToNode.get(cluster.id).adjList.add(pair);
//                }
//            }
//        }
//        for (Pair<Node, Double> n: nodes.get(0).adjList)
//        {
//
//            System.out.println(n.first.id);
//            System.out.println(n.second);
//        }
//    }
//}
//class MapJson
//{
//    ArrayList<Cluster> elements;
//}
//class Cluster
//{
//    String type;
//    long id;
//    ArrayList<Long> nodes;
//    Bound bounds;
//    class Bound
//    {
//        double minlat;
//        double minlon;
//        double maxlat;
//        double maxlon;
//    }
//}
//class Node
//{
//    long val;
//    long id;
//    ArrayList<Pair<Node, Double>> adjList;
//    Pair<Double, Double> location;
//
//    public Node(long id, long val, ArrayList<Pair<Node, Double>> adjList, Pair<Double, Double> location)
//    {
//        this.id = id;
//        this.val = val;
//        this.adjList = adjList;
//        this.location = location;
//    }
//}
//class Pair<t,p>
//{
//    t first;
//    p second;
//    public Pair(t first, p second)
//    {
//        this.first = first;
//        this.second = second;
//    }
//}
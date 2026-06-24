package aoc.dia10.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ButtonSolver {

    public static int minPresses(Machine m) {
        int[] dist = new int[1 << m.lights()];
        Arrays.fill(dist, -1);
        Queue<Integer> q = new LinkedList<>();
        dist[0] = 0; q.offer(0);
        return search(m.target(), m.buttons(), dist, q);
    }

    private static int search(int target, java.util.List<Integer> buttons, int[] dist, Queue<Integer> q) {
        while (!q.isEmpty()) {
            int s = q.poll();
            if (s == target) return dist[s];
            for (int btn : buttons) tryVisit(dist, q, s ^ btn, dist[s] + 1);
        }
        return -1;
    }

    private static void tryVisit(int[] dist, Queue<Integer> q, int state, int d) {
        if (dist[state] == -1) { dist[state] = d; q.offer(state); }
    }
}

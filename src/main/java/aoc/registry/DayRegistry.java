package aoc.registry;

import aoc.core.Day;
import java.util.Map;

public class DayRegistry {

    private final Map<Integer, Day> days;

    public DayRegistry(Map<Integer, Day> days){
        this.days = days;
    }

    public Day get(int day){
        return days.get(day);
    }

}

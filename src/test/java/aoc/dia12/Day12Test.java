package aoc.dia12;

import aoc.dia12.adapter.PuzzleAdapter;
import aoc.dia12.model.Packer;
import aoc.dia12.model.PuzzleInput;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Day12Test {

    @Test
    void adapterParseaFigurasYRegiones() {
        PuzzleInput input = PuzzleAdapter.parse(TestInputs.day(12));
        assertTrue(input.shapes().length > 0);
        assertFalse(input.regions().isEmpty());
    }

    @Test
    void parte1InputReal() {
        Day12 day = new Day12();
        PuzzleInput input = day.parse(TestInputs.day(12));
        assertEquals(541L, day.part1(input));
    }

    @Test
    void parte2EstrellaGratis() {
        Day12 day = new Day12();
        PuzzleInput input = day.parse(TestInputs.day(12));
        assertEquals("Estrella gratis (se obtiene completando los demás días)", day.part2(input));
    }

    @Test
    void packerEncajaRegionesDelInput() {
        PuzzleInput input = PuzzleAdapter.parse(TestInputs.day(12));
        long fitting = input.regions().stream().filter(r -> Packer.fits(r, input.shapes())).count();
        assertEquals(541L, fitting);
    }
}

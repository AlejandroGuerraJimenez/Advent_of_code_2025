# Reto Día 10 — Factory

Documentación de la arquitectura y los principios de diseño aplicados en la
resolución del Día 10 de Advent of Code 2025.

---

## 1. El problema

Cada máquina de la fábrica tiene un **diagrama de indicadores luminosos** y un
conjunto de **botones**. Cada botón lleva una lista de índices que indica qué
luces afecta.

### Parte 1 — Mínimas pulsaciones para configurar luces

Cada botón **alterna** (XOR) las luces de su lista. El estado inicial es todo
apagado; el objetivo es alcanzar el patrón del diagrama con el **menor número
total de pulsaciones** sumando todos los botones de todas las máquinas.

Ejemplo:

```
[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1)
```

La respuesta es la suma del mínimo de pulsaciones de cada máquina.

### Parte 2 — Mínimas pulsaciones para configurar voltaje (joltage)

El mismo conjunto de botones, pero ahora actúan en modo **contador**: cada
pulsación del botón `j` **incrementa en 1** cada contador `i` que aparezca en
su lista. Los contadores parten de cero y deben alcanzar exactamente los valores
`{b₀, b₁, …, b_{k-1}}` indicados al final de cada línea.

Ejemplo para la primera máquina del ejemplo: `{3,5,4,7}` → 10 pulsaciones.
Total del ejemplo (3 máquinas) → **33**.

---

## 2. Estructura de paquetes

```
aoc.dia10
├── Day10.java          ← Entry point del día
├── Parser.java         ← Parsea una línea en Machine
└── model/
    ├── Machine.java    record(lights, target, buttons, joltages)
    ├── ButtonSolver.java   BFS bitmask  → Parte 1
    └── JoltageSolver.java  ILP exacto   → Parte 2
```

---

## 3. Flujo de ejecución

### Parte 1 — BFS sobre espacio de estados (bitmask)

```
input (String)
  → Parser.parse()              List<Machine>
  → ButtonSolver.minPresses()   BFS: estado=bitmask de luces
       estado inicial  = 0b000…0
       estado objetivo = target (bitmask del diagrama)
       vecinos de s    = s XOR buttons[j]  ∀j
       distancia mínima a target
  → sum()
  → String (respuesta Parte 1)
```

### Parte 2 — Branch-and-Bound ILP con simplex racional exacto

```
input (String)
  → Parser.parse()                List<Machine>
  → JoltageSolver.minPresses()
       Construir A (0/1), b (joltages), c = (1,…,1)
       bb(A, b, c, k, n, 0):
         solveLP()  →  relajación LP con simplex bifásico exacto
         fracVar()  →  ¿hay variable fraccionaria?
           no → solución entera, retornar lbSum + Σcⱼxⱼ
           sí → branch en xⱼ:
                  branchRight: xⱼ ≥ ⌈xⱼ⌉  (sustituir, ajustar b)
                  branchLeft:  xⱼ ≤ ⌊xⱼ⌋  (añadir restricción de cota)
                  min(right, left)
  → sum()
  → String (respuesta Parte 2)
```

---

## 4. Explicación clase a clase

### `Machine` — Value Object

```java
public record Machine(int lights, int target, List<Integer> buttons, List<Integer> joltages) {}
```

`lights` y `target` sirven para la Parte 1. `buttons` es compartida por ambas
partes (como bitmask para Parte 1, como columnas de la matriz A para Parte 2).
`joltages` es exclusiva de la Parte 2.

---

### `Parser` — traducción de texto a dominio

**Diagrama** `[.##.]`: cada `#` activa el bit correspondiente de `target`.

```java
private static int toTargetMask(String diagram) {
    int mask = 0;
    for (int i = 0; i < diagram.length(); i++)
        if (diagram.charAt(i) == '#') mask |= (1 << i);
    return mask;
}
```

**Botones** `(1,3)`: cada índice activa su bit en el bitmask del botón.

```java
private static int toButtonMask(String spec) {
    int mask = 0;
    for (String part : spec.split(","))
        mask |= (1 << Integer.parseInt(part.trim()));
    return mask;
}
```

El patrón regex `\(([^)]+)\)` captura todos los bloques `(…)` de la línea,
lo que extrae los botones sin confundirse con las llaves `{…}` de los joltages.

**Joltages** `{3,5,4,7}`:

```java
private static List<Integer> extractJoltages(String line) {
    String inner = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
    return Arrays.stream(inner.split(",")).map(s -> Integer.parseInt(s.trim())).toList();
}
```

---

### `ButtonSolver` — Parte 1: BFS sobre bitmask

El estado de la máquina es el bitmask de los `n` indicadores (apagado=0,
encendido=1). Con `n ≤ 13` caben en un `int`. El espacio de estados es
`2ⁿ ≤ 8192` nodos.

```java
public static int minPresses(Machine m) {
    int[] dist = new int[1 << m.lights()];
    Arrays.fill(dist, -1);
    Queue<Integer> q = new LinkedList<>();
    dist[0] = 0; q.offer(0);
    return search(m.target(), m.buttons(), dist, q);
}
```

La vecindad de un estado `s` es `{ s XOR btn | btn ∈ buttons }`. El BFS
explora en anchura, garantizando la distancia mínima al estado objetivo.

- **Complejidad**: O(2ⁿ · |buttons|). Máximo ≈ 8192 × 13 ≈ 100k operaciones.
- **Correctitud**: BFS sobre grafos no ponderados siempre devuelve la distancia
  mínima.

---

### `JoltageSolver` — Parte 2: ILP exacto con Branch-and-Bound

#### Formulación matemática

Para cada máquina con `k` contadores y `n` botones, se plantea el
**Problema de Programación Lineal Entera (ILP)**:

```
minimizar   Σⱼ xⱼ
sujeto a    A · x = b
            x ≥ 0,  x ∈ ℤⁿ
```

donde `A[i][j] = 1` si el botón `j` afecta al contador `i` (bit `i` del
bitmask del botón `j`), y `b[i]` es el joltage objetivo del contador `i`.

La matriz `A` es binaria (0/1) y tiene tamaño `k × n` (k ≤ 13, n ≤ 13).

#### Aritmética racional exacta — clase interna `Q`

Para evitar errores de punto flotante (que producían respuestas incorrectas
en casos con bases degeneradas), todas las operaciones del simplex se realizan
con **fracciones exactas** usando `BigInteger`:

```java
private static final class Q {
    final BigInteger n, d; // d > 0, mcd(|n|, d) = 1

    static Q make(BigInteger n, BigInteger d) {
        if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
        if (n.signum() == 0) return ZERO;
        BigInteger g = n.abs().gcd(d);
        return new Q(n.divide(g), d.divide(g));
    }
    // add, sub, mul, div con reducción por mcd
}
```

Cada operación aritmética reduce la fracción por el MCD, manteniendo los
numeradores y denominadores acotados (≤ det(base) × max(b) ≈ 10⁶ × 300).

#### El simplex bifásico

El LP con `k` restricciones de igualdad se resuelve con el **método simplex
bifásico en forma tabular**:

| Fase | Objetivo | Variables entrantes |
|------|----------|---------------------|
| 1 | minimizar Σ artificiales | todas (`maxCol = cols`) |
| 2 | minimizar c·x | solo originales (`maxCol = n`) |

**Tableau inicial** (k+1 filas × cols+1 columnas):
- Columnas 0..n-1: variables originales xⱼ
- Columnas n..cols-1: artificiales yᵢ (base inicial, coste 1 en Fase 1)
- Columna `cols`: RHS (b[i])
- Fila k: fila objetivo (costes reducidos)

**Regla de Bland** para la variable entrante (evita ciclos):
```java
private static int enter(Q[][] tab, int k, int maxCol) {
    for (int j = 0; j < maxCol; j++) if (tab[k][j].neg()) return j;
    return -1;
}
```

#### La corrección crítica: expulsión de artificiales degenerados

Cuando el sistema tiene más restricciones que variables (`k > n`) o hay
restricciones linealmente dependientes, la Fase 1 puede terminar con
**artificiales degenerados en la base** — básicos a valor 0. Si no se expulsan
antes de la Fase 2, los pivots posteriores los pueden hacer negativos,
violando las restricciones de igualdad A·x = b (la Fase 2 estaría resolviendo
un sistema diferente al original).

```java
private static void expelArtificials(Q[][] tab, int[] basis, int k, int n, int cols) {
    for (int i = 0; i < k; i++) {
        if (basis[i] < n) continue; // ya es variable original
        // Pivota el artificial fuera usando la primera variable original
        // que tenga coeficiente no nulo en esta fila
        int pivot = -1;
        for (int j = 0; j < n; j++) {
            if (!tab[i][j].zero()) { pivot = j; break; }
        }
        if (pivot >= 0) pivotRow(tab, basis, k, cols, i, pivot);
        // Si pivot == -1: restricción redundante, se deja en base a 0
        // (setupP2 la ignora porque basis[i] >= n)
    }
}
```

Sin este paso, machine 3 (9 restricciones, 8 botones) reportaba 189 pulsaciones
cuando la única solución entera tiene suma 201. Con el paso, el LP
devuelve correctamente la solución {3,120,0,19,6,20,13,20} (Σ = 201).

#### Branch-and-Bound

El B&B ramifica en la primera variable fraccionaria con coste positivo:

```
bb(A, b, c, k, n, lbSum):
  x = solveLP(A, b, c, k, n)   // relajación LP exacta
  si x es null → infeasible, devolver MAX_VALUE
  fj = primera j con c[j]>0 y x[j] ∉ ℤ
  si fj == -1  → solución entera, devolver lbSum + Σcⱼxⱼ
  sino:
    fv = ⌊x[fj]⌋
    right = branchRight: xⱼ ≥ fv+1
              sustitución x'[fj] = xⱼ-(fv+1) ≥ 0
              b' = b - A[:,fj]·(fv+1)
              bb(A, b', c, k, n, lbSum + fv+1)
    left  = branchLeft:  xⱼ ≤ fv
              nueva restricción: xⱼ + s = fv
              AL = A aumentada con fila [0…1ⱼ…0 | 0…0 1ₛ], bL[k] = fv
              bb(AL, bL, cL, k+1, n+1, lbSum)
    devolver min(right, left)
```

- `branchRight` fija una cota inferior mediante sustitución de variable (sin
  añadir filas a A).
- `branchLeft` añade una restricción de cota superior como nueva fila de
  igualdad (con variable de holgura `s`).
- Sin poda: el árbol siempre explora ambas ramas, garantizando el óptimo global.

---

## 5. Decisiones de diseño

### Por qué aritmética exacta (BigInteger) y no `double`

Los errores de punto flotante en el simplex producían tres síntomas distintos
según la regla de pivoteo usada:

| Variante | Resultado | Diferencia del correcto |
|----------|-----------|------------------------|
| `double` más-negativo | 16657 | −6 |
| `double` Bland | 16637 | −26 |
| `BigInteger` exacto sin expulsión | 16643 | −20 |
| **`BigInteger` exacto con expulsión** | **16663** | **0** |

El `double` puede reportar artificiales degenerados como "correctamente en 0"
cuando en realidad hay un error numérico de orden 10⁻¹⁴, pasando el test
de infeasibilidad. Con `BigInteger`, el valor es 0/1 o no-0/1 de forma exacta.

### Por qué Branch-and-Bound y no simplex puro

La relajación LP de muchas máquinas tiene solución fraccionaria. Simplemente
redondear el óptimo LP es incorrecto: machine 78 tiene LP ≈ 63.5 pero
ILP = 76. El B&B garantiza el óptimo entero al explorar exhaustivamente el
árbol de soluciones.

### Separación Parte 1 / Parte 2

`ButtonSolver` y `JoltageSolver` son dos estrategias completamente
independientes que comparten el mismo modelo (`Machine`). `Day10` delega a
cada uno según la parte, sin lógica propia.

---

## 6. Resultados

| Parte | Respuesta | Algoritmo | Complejidad |
|-------|-----------|-----------|-------------|
| 1 | **415** | BFS bitmask | O(2ⁿ · |buttons|) por máquina |
| 2 | **16663** | B&B + simplex Q exacto | exponencial acotado en k,n ≤ 13 |

---

## Mejoras arquitectónicas aplicadas

### Fase 1 — Core: interfaz `Day<T>` con parseo único
- `Day10` implementa ahora `Day<List<Machine>>`: las máquinas se **parsean una
  sola vez** y ambas partes operan sobre la lista (antes cada parte llamaba a
  `Parser.parse` con la lógica de stream inline).
- `part1` delega en `ButtonSolver` y `part2` en `JoltageSolver` sobre el mismo
  modelo.
- `part1`/`part2` devuelven `Object`; el `toString` lo hace `DayRunner`.
- Se añadió `number()`. La salida muestra la etiqueta y el resultado de cada parte.

### Fase 2 — Utilidades de parseo (`aoc.parse`)
- `Parser` usa `Lines.nonBlank(input)`; el resto del parseo (regex de botones,
  máscaras, joltages) se mantiene por ser específico del formato del día.

### Enriquecimiento local — `optimization/`
- **`JoltageSolver`** reducido a fachada (~25 líneas): adapta `Machine` → matrices
  ILP y delega en `BranchAndBoundSolver`.
- **`optimization/Rational`**: fracciones exactas (antes inner class `Q`).
- **`optimization/SimplexSolver`**: simplex de dos fases + expulsión de artificiales.
- **`optimization/BranchAndBoundSolver`**: ramificación entera sobre el LP relajado.
- Tests en `src/test/java/aoc/dia10/Day10Test.java` (415 / 16663).

#### Justificación

**Por qué enriquecer este día.** `JoltageSolver` concentraba ~260 líneas y cuatro
responsabilidades distintas: fracciones exactas (`Q`), simplex de dos fases,
branch-and-bound y traducción `Machine` → matrices ILP. Era el monolito más claro
del proyecto; cualquier cambio en el LP o el B&B obligaba a navegar un solo fichero.

**Por qué `optimization/`.** Las tres clases extraídas forman una familia técnica
propia del puzzle (programación lineal entera), no del dominio “máquina de luces”.
`ButtonSolver` (BFS bitmask, parte 1) se queda en `model/` porque es otro algoritmo
con otra razón de cambio. `optimization/` agrupa LP + B&B sin sacarlos a un paquete
global: son infraestructura *de este día*, no reutilizable en otros puzzles.

**Por qué no subir `Rational` a `aoc.parse` o similar.** Las fracciones exactas solo
las usa el simplex del día 10; extraerlas al shared kernel añadiría acoplamiento
falso. El criterio acordado fue enriquecer **dentro del día**, no globalizar.

**Por qué `JoltageSolver` como fachada.** `Day10.part2` solo necesita
`minPresses(Machine)`; ocultar simplex + B&B detrás de esa API mantiene el contrato
estable y concentra la adaptación del puzzle al ILP en un sitio pequeño (~25 líneas).

**Patrón aplicado.** Facade sobre tres componentes con SRP: `Rational` (aritmética),
`SimplexSolver` (relajación LP), `BranchAndBoundSolver` (enteridad). Misma
separación que en diseño de optimización clásico, pero acotada a `dia10`.

**Tests.** El simplex con expulsión de artificiales es frágil; los tests fijan
**415** (parte 1, sin cambios) y **16663** (parte 2) sobre el input real para
detectar regresiones numéricas.

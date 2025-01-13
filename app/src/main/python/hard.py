import numpy as np
import os
import random
from java.util import ArrayList

# Formas de las tablas Q
MOVE_Q_SHAPE = (10, 10, 101)  # Para movimiento
SHOOT_Q_SHAPE = (10, 10, 8, 10)  # Para disparos

# Hiperparámetros
EPSILON = 0.1  # Tasa de exploración
ALPHA = 0.1  # Tasa de aprendizaje
GAMMA = 0.9  # Factor de descuento

# Tablas Q globales
q_table_move = None
q_table_shoot = None

# Directorio base para guardar archivos
BASE_DIR = None  # Será inicializado desde Kotlin


def set_base_dir(base_dir):
    global BASE_DIR
    BASE_DIR = base_dir
    print(f"[DEBUG] Base directory set to: {BASE_DIR}")


# Archivos para guardar las tablas Q
def get_move_q_file():
    return os.path.join(BASE_DIR, 'q_table_move.npy')


def get_shoot_q_file():
    return os.path.join(BASE_DIR, 'q_table_shoot.npy')


# Inicialización de tablas Q
def load_or_initialize_q_table(file_path, shape):
    """Cargar o inicializar una tabla Q."""
    if os.path.exists(file_path):
        print(f"[DEBUG] Loading Q-table from {file_path}")
        return np.load(file_path)
    else:
        print(f"[DEBUG] Initializing a new Q-table with shape {shape}")
        return np.zeros(shape)


# Inicializar las tablas Q globales
def initialize_q_tables():
    global q_table_move, q_table_shoot
    print("[DEBUG] Initializing Q-tables")
    q_table_move = load_or_initialize_q_table(get_move_q_file(), MOVE_Q_SHAPE)
    q_table_shoot = load_or_initialize_q_table(get_shoot_q_file(), SHOOT_Q_SHAPE)
    print("[DEBUG] Q-tables initialized")


# Guardar tablas Q
def save_q_table(q_table, file_path):
    """Guardar la tabla Q en un archivo."""
    print(f"[DEBUG] Saving Q-table to {file_path}")
    np.save(file_path, q_table)


def choose_shot(q_table, current_position, wind_direction, wind_strength, grid_state):
    # Convertir la dirección del viento a índices manejables
    wind_map = {
        "N": 0, "S": 1, "E": 2, "W": 3,
        "NE": 4, "NW": 5, "SE": 6, "SW": 7
    }
    wind_index = wind_map.get(wind_direction, -1)

    # Representar el estado actual
    state = (current_position[0], current_position[1], wind_index, wind_strength)

    print(f"[DEBUG] Choosing shot for state {state}")

    if random.random() < EPSILON:
        print(f"[DEBUG] Exploring: Choosing random cell from grid")
        available_cells = [(i, j) for i, row in enumerate(grid_state) for j, cell in enumerate(row)]
        chosen_cell = list(random.choice(available_cells))
        result = ArrayList()
        result.add(chosen_cell[0])
        result.add(chosen_cell[1])
        return result

    # Evaluar las Q-values
    q_values = [
        q_table[i, j, wind_index, wind_strength]
        for i, row in enumerate(grid_state)
        for j, cell in enumerate(row)
    ]
    print(f"[DEBUG] Exploiting: Q-values for available cells: {q_values}")
    best_index = np.argmax(q_values)
    best_cell = list(
        divmod(best_index, len(grid_state[0])))  # Convertir índice lineal a coordenadas
    print(f"[DEBUG] Best Q-value: {q_values[best_index]} at {best_cell}")
    result = ArrayList()
    result.add(int(best_cell[0]))
    result.add(int(best_cell[1]))
    return result


def update_shoot_q_table(q_table, current_position, wind_direction, wind_strength, target_position,
                         reward):
    print(f"[DEBUG] Updating shoot Q-table")
    # Convertir la dirección del viento a índices manejables
    wind_map = {
        "N": 0, "S": 1, "E": 2, "W": 3,
        "NE": 4, "NW": 5, "SE": 6, "SW": 7
    }
    wind_index = wind_map.get(wind_direction, -1)

    # Estado actual y próximo
    state = (current_position[0], current_position[1], wind_index, wind_strength)
    next_state = (target_position[0], target_position[1])

    # Q-learning update
    old_q = q_table[state]
    best_next_q = np.max(q_table[next_state])
    q_table[state] += ALPHA * (reward + GAMMA * best_next_q - old_q)

    print(f"[DEBUG] Updated Q-value for state {state}: {old_q} -> {q_table[state]}")

    # Guardar la tabla Q actualizada
    save_q_table(q_table, get_shoot_q_file())


def choose_move(q_table, current_position, fuel, valid_cells):
    print(f"[DEBUG] Choosing move for position: {current_position} with fuel: {fuel}")
    print(f"[DEBUG] Valid cells: {valid_cells}")

    # Representar el estado actual
    state = (current_position[0], current_position[1], fuel)

    print(f"[DEBUG] Choosing move for state {state}")

    if random.random() < EPSILON:
        print(f"[DEBUG] Exploring: Choosing random cell from ${valid_cells}")
        chosen_cell = list(random.choice(valid_cells))
        print(f"[DEBUG] Chosen cell: {chosen_cell}")
        result = ArrayList()
        result.add(chosen_cell[0])
        result.add(chosen_cell[1])
        return result

    # Evaluar las Q-values
    q_values = [q_table[cell[0], cell[1], fuel] for cell in valid_cells]
    print(f"[DEBUG] Exploiting: Q-values for valid cells: {q_values}")
    chosen_cell = list(valid_cells[np.argmax(q_values)])
    print(f"[DEBUG] Chosen cell: {chosen_cell}")
    result = ArrayList()
    result.add(int(chosen_cell[0]))
    result.add(int(chosen_cell[1]))
    return result


def update_move_q_table(q_table, current_position, fuel, next_position, reward):
    print(f"[DEBUG] Updating move Q-table")

    # Estado actual y próximo
    state = (current_position[0], current_position[1], fuel)
    next_state = (next_position[0], next_position[1], max(fuel - 1, 0))

    # Q-learning update
    old_q = q_table[state]
    best_next_q = np.max(q_table[next_state])
    q_table[state] += ALPHA * (reward + GAMMA * best_next_q - old_q)

    print(f"[DEBUG] Updated Q-value for state {state}: {old_q} -> {q_table[state]}")

    # Guardar la tabla Q actualizada
    save_q_table(q_table, get_move_q_file())

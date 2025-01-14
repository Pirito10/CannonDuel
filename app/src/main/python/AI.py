import numpy as np
import os
import random
from java.util import ArrayList

# Formas de las tablas Q
SHOOT_Q_SHAPE = (6, 6, 6, 6, 4, 3, 7, 4, 2, 6, 6, 3)
MOVE_Q_SHAPE = (6, 6, 21, 6, 6)

# Hiperparámetros
EPSILON = 0.1  # Tasa de exploración
ALPHA = 0.1  # Tasa de aprendizaje
GAMMA = 0.9  # Factor de descuento

# Tablas Q globales
q_table_move = None
q_table_shoot = None

# Directorio base para guardar archivos
BASE_DIR = None  # Será inicializado desde Kotlin


# Función para establecer el directorio de ficheros de la aplicación
def set_base_dir(base_dir):
    global BASE_DIR
    BASE_DIR = base_dir
    print(f"[DEBUG] Base directory set to: {BASE_DIR}")


# Función para obtener la tabla Q de disparos
def get_shoot_q_file():
    return os.path.join(BASE_DIR, 'q_table_shoot.npy')


# Función para obtener la tabla Q de movimientos
def get_move_q_file():
    return os.path.join(BASE_DIR, 'q_table_move.npy')


# Función para inicializar las tablas Q
def initialize_q_tables():
    global q_table_move, q_table_shoot
    print("[DEBUG] Initializing Q-tables")
    q_table_move = load_or_initialize_q_table(get_move_q_file(), MOVE_Q_SHAPE)
    q_table_shoot = load_or_initialize_q_table(get_shoot_q_file(), SHOOT_Q_SHAPE)
    print("[DEBUG] Q-tables initialized")


# Función para cargar o crear las tablas Q
def load_or_initialize_q_table(file_path, shape):
    if os.path.exists(file_path):
        print(f"[DEBUG] Loading Q-table from {file_path}")
        return np.load(file_path)
    else:
        print(f"[DEBUG] Initializing a new Q-table with shape {shape}")
        return np.zeros(shape, dtype=np.float32)


# Función para guardar las tablas Q
def save_q_table(q_table, file_path):
    print(f"[DEBUG] Saving Q-table to {file_path}")
    np.save(file_path, q_table)


# Función para seleccionar qué casilla disparar
def choose_shot(q_table, current_position, enemy_position, wind_direction, wind_strength,
                ammo_counts):
    # Convertir la dirección del viento a índices manejables
    wind_map = {"N": 0, "S": 1, "E": 2, "W": 3}
    wind_index = wind_map.get(wind_direction, -1)

    # Estado actual
    state = (
        current_position[0], current_position[1],
        enemy_position[0], enemy_position[1],
        wind_index, wind_strength,
        ammo_counts[0], ammo_counts[1], ammo_counts[2]
    )

    print(f"[DEBUG] Choosing shot for state {state}")

    # Exploración
    if random.random() < EPSILON:
        print(f"[DEBUG] Exploring: Choosing random cell from grid")
        available_cells = [(i, j) for i in range(6) for j in range(6)]
        available_ammo = [0, 1, 2]
        chosen_cell = list(random.choice(available_cells))
        chosen_ammo = random.choice(available_ammo)
        result = ArrayList()
        result.add(chosen_cell[0])
        result.add(chosen_cell[1])
        result.add(chosen_ammo)
        return result

    # Explotación
    print(f"[DEBUG] Exploiting: Evaluating Q-values for actions")
    best_q_value = -float('inf')
    best_action = None

    for ammo_type in range(3):  # Tipos de munición
        if ammo_counts[ammo_type] > 0:  # Solo considerar munición disponible
            for i in range(6):
                for j in range(6):
                    # Calcular el Q-value para el estado y acción actual
                    action = (i, j, ammo_type)
                    full_state_action = state + action
                    q_value = q_table[full_state_action]

                    if q_value > best_q_value:
                        best_q_value = q_value
                        best_action = action

    print(f"[DEBUG] Best action: {best_action} with Q-value: {best_q_value}")
    result = ArrayList()
    result.add(best_action[0])
    result.add(best_action[1])
    result.add(best_action[2])
    return result


# Función para actualizar la tabla Q de disparos
def update_shoot_q_table(q_table, current_position, enemy_position, wind_direction, wind_strength,
                         ammo_counts, target_position, ammo_type, reward):
    print(f"[DEBUG] Updating shoot Q-table")
    # Convertir la dirección del viento a índices manejables
    wind_map = {"N": 0, "S": 1, "E": 2, "W": 3}
    wind_index = wind_map.get(wind_direction, -1)

    # Estado actual y próximo
    state = (
        current_position[0], current_position[1],
        enemy_position[0], enemy_position[1],
        wind_index, wind_strength,
        ammo_counts[0], ammo_counts[1], ammo_counts[2]
    )

    # Acción actual (posición objetivo y tipo de munición)
    action = (target_position[0], target_position[1], ammo_type)

    # Estado y acción combinados para acceder a la tabla Q de 12 dimensiones
    full_state_action = state + action

    # Determinar el estado siguiente
    next_ammo_counts = list(ammo_counts)
    next_ammo_counts[ammo_type] += 1
    next_state = (
        target_position[0], target_position[1],
        enemy_position[0], enemy_position[1],
        wind_index, wind_strength,
        next_ammo_counts[0], next_ammo_counts[1], next_ammo_counts[2]
    )

    # Encontrar el mejor Q-value para el estado siguiente
    best_next_q = -float("inf")
    for next_ammo_type in range(3):
        for i in range(6):
            for j in range(6):
                next_full_state_action = next_state + (i, j, next_ammo_type)
                best_next_q = max(best_next_q, q_table[next_full_state_action])

    # Q-learning update
    old_q = q_table[full_state_action]
    q_table[full_state_action] += ALPHA * (reward + GAMMA * best_next_q - old_q)

    print(
        f"[DEBUG] Updated Q-value for state-action {full_state_action}: {old_q} -> {q_table[full_state_action]}")

    # Guardar la tabla Q actualizada
    save_q_table(q_table, get_shoot_q_file())


# Función para seleccionar a qué casilla moverse
def choose_move(q_table, current_position, fuel, valid_cells):
    print(f"[DEBUG] Choosing move for position: {current_position} with fuel: {fuel}")
    print(f"[DEBUG] Valid cells: {valid_cells}")

    # Estado actual
    state = (current_position[0], current_position[1], fuel)

    print(f"[DEBUG] Choosing move for state {state}")

    # Exploración
    if random.random() < EPSILON:
        print(f"[DEBUG] Exploring: Choosing random cell from ${valid_cells}")
        chosen_cell = list(random.choice(valid_cells))
        print(f"[DEBUG] Chosen cell: {chosen_cell}")
        result = ArrayList()
        result.add(chosen_cell[0])
        result.add(chosen_cell[1])
        return result

    # Explotación
    print(f"[DEBUG] Exploiting: Evaluating Q-values for valid cells")
    best_q_value = -float("inf")
    best_cell = None

    for cell in valid_cells:
        # Estado-acción para la tabla Q
        action = (cell[0], cell[1])
        full_state_action = state + action

        # Obtener el Q-value correspondiente
        q_value = q_table[full_state_action]

        # Actualizar si encontramos un mejor valor
        if q_value > best_q_value:
            best_q_value = q_value
            best_cell = cell

    print(f"[DEBUG] Best cell: {best_cell} with Q-value: {best_q_value}")
    result = ArrayList()
    result.add(best_cell[0])
    result.add(best_cell[1])
    return result


# Función para actualizar la tabla Q de movimientos
def update_move_q_table(q_table, current_position, fuel, target_position, reward):
    print(f"[DEBUG] Updating move Q-table")

    # Estado actual
    state = (current_position[0], current_position[1], fuel)

    # Acción actual (posición objetivo)
    action = (target_position[0], target_position[1])

    # Estado y acción combinados para acceder a la tabla Q
    full_state_action = state + action

    # Determinar el estado siguiente
    next_state = (target_position[0], target_position[1], fuel)

    # Encontrar el mejor Q-value para el estado siguiente
    best_next_q = -float("inf")
    for i in range(6):  # Grid de 6x6
        for j in range(6):
            next_full_state_action = next_state + (i, j)
            best_next_q = max(best_next_q, q_table[next_full_state_action])

    # Q-learning update
    old_q = q_table[full_state_action]
    q_table[full_state_action] += ALPHA * (reward + GAMMA * best_next_q - old_q)

    print(
        f"[DEBUG] Updated Q-value for state-action {full_state_action}: {old_q} -> {q_table[full_state_action]}")

    # Guardar la tabla Q actualizada
    save_q_table(q_table, get_move_q_file())

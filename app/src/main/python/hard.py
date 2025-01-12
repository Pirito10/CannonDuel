import random
import numpy as np
import os

class Agent:
    def __init__(self, q_table_move_file='q_table_move.npy', q_table_shoot_file='q_table_shoot.npy'):
        self.q_table_move_file = q_table_move_file
        self.q_table_shoot_file = q_table_shoot_file
        self.q_table_move = self.load_q_table(q_table_move_file, (10, 10, 100))  # Q-table for move
        self.q_table_shoot = self.load_q_table(q_table_shoot_file, (10, 10, 8))  # Q-table for shoot
        self.epsilon = 0.1  # Exploration rate
        self.alpha = 0.1    # Learning rate
        self.gamma = 0.9    # Discount factor

    def load_q_table(self, file_path, shape):
        if os.path.exists(file_path):
            return np.load(file_path)
        else:
            return np.zeros(shape)

    def save_q_table(self, q_table, file_path):
        np.save(file_path, q_table)

    def move(self, available_cells, fuel, current_position):
        state = (current_position[0], current_position[1], fuel)
        if random.uniform(0, 1) < self.epsilon:
            return random.choice(available_cells)
        q_values = [self.q_table_move[cell[0], cell[1], fuel] for cell in available_cells]
        return available_cells[np.argmax(q_values)]

    def shoot(self, wind_direction, available_cells):
        state = (wind_direction[0], wind_direction[1])
        if random.uniform(0, 1) < self.epsilon:
            return random.choice(available_cells)
        q_values = [self.q_table_shoot[cell[0], cell[1], wind_direction] for cell in available_cells]
        return available_cells[np.argmax(q_values)]

    def update_move_q_table(self, current_position, fuel, next_position, reward):
        state = (current_position[0], current_position[1], fuel)
        next_state = (next_position[0], next_position[1], fuel - 1)
        best_next_q = np.max(self.q_table_move[next_state[0], next_state[1], fuel - 1])
        self.q_table_move[state[0], state[1], fuel] += self.alpha * (reward + self.gamma * best_next_q - self.q_table_move[state[0], state[1], fuel])
        self.save_q_table(self.q_table_move, self.q_table_move_file)

    def update_shoot_q_table(self, wind_direction, target_position, reward):
        state = (wind_direction[0], wind_direction[1])
        best_next_q = np.max(self.q_table_shoot[target_position[0], target_position[1], wind_direction])
        self.q_table_shoot[state[0], state[1], wind_direction] += self.alpha * (reward + self.gamma * best_next_q - self.q_table_shoot[state[0], state[1], wind_direction])
        self.save_q_table(self.q_table_shoot, self.q_table_shoot_file)

if __name__ == "__main__":
    agent = Agent()
    # Example usage
    available_cells = [(i, j) for i in range(10) for j in range(10)]
    # Simulate some moves and updates
    current_position = (0, 0)
    fuel = 100
    next_position = (1, 0)
    reward = 1 # hay que cambiar esta línea para que el reward sea en base a si le das al enemigo o no. A implementar cuando se juntr el código de
    agent.update_move_q_table(current_position, fuel, next_position, reward)
    wind_direction = (0, 1)
    target_position = (1, 1)
    agent.update_shoot_q_table(wind_direction, target_position, reward)
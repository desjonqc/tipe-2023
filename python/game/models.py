import numpy as np
class BoardPosition:
    def __init__(self, position: np.ndarray):
        self.position = position

    @staticmethod
    def from_bytes(position: bytes):
        return BoardPosition(np.frombuffer(position, dtype=np.dtype(float)))

class PositionResult:
    def __init__(self, angle: int, norm: int, result: int):
        self.angle = angle
        self.norm = norm
        self.result = result

    @staticmethod
    def from_bytes(result: bytes):
        return PositionResult(*np.frombuffer(result, dtype=np.dtype(int)))


class FullPosition:


def read_full_positions(file_name: str, size: int):
    with open(file_name, 'rb') as file:
        positions = []
        
        while byte := file.read(size):
            positions.append(BoardPosition.from_bytes(byte))
        return positions


read_full_positions("datastored/test/0.data", 8)


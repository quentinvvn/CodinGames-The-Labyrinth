import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int rows = in.nextInt(); // number of rows.
		int columns = in.nextInt(); // number of columns.
		int A = in.nextInt(); // number of rounds between the time the alarm countdown is activated and the
								// time the alarm goes off.

		Labyrinth labyrinth = null;
		boolean controlRoomFound = false;
		boolean wayBackFound = false;
		boolean alarmTriggered = false;
		Direction direction = null;

		// game loop
		while (true) {
			int kirkY = in.nextInt(); // row where Kirk is located.
			int kirkX = in.nextInt(); // column where Kirk is located.
			Position kirkPosition = new Position(kirkX, kirkY);
			Position controlRoomPosition = null;

			char[][] labyrinthAsCharArray = new char[rows][columns];
			for (int i = 0; i < rows; i++) {
				labyrinthAsCharArray[i] = in.next().toCharArray();

				for (int j = 0; j < columns; j++) {
					if (labyrinthAsCharArray[i][j] == 'C') {
						controlRoomFound = true;
						controlRoomPosition = new Position(j, i);
					}
				}
			}

			if (labyrinthAsCharArray[kirkPosition.y][kirkPosition.x] == 'C') {
				alarmTriggered = true;
			}

			if (labyrinth == null) {
				labyrinth = new Labyrinth(labyrinthAsCharArray, rows, columns);
			} else {
				labyrinth.discoverNewCells(labyrinthAsCharArray, direction, kirkPosition);
			}

			if (controlRoomFound && !wayBackFound) {
				int turnCount = labyrinth.computeFastestWayTo(controlRoomPosition, CellType.START);
				if (turnCount != -1 && turnCount <= A) {
					wayBackFound = true;
				}
			}

			CellType targetType;
			if (wayBackFound && alarmTriggered) {
				targetType = CellType.START;
			} else if (controlRoomFound && wayBackFound) {
				targetType = CellType.CONTROL_ROOM;
			} else {
				targetType = CellType.UNKNOWN;
			}
			boolean ignoreUnknownCases = (targetType != CellType.UNKNOWN);
			Cell nextCell = labyrinth.findNextCellLeadingTo(kirkPosition, targetType, ignoreUnknownCases);
			direction = kirkPosition.computeDirectionTo(nextCell.position);

			System.out.println(direction);
		}
    }
}

class Position {
	int x, y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Direction computeDirectionTo(Position position) {
		if (position.x > this.x) {
			return Direction.RIGHT;
		} else if (position.x < this.x) {
			return Direction.LEFT;
		} else if (position.y > this.y) {
			return Direction.DOWN;
		} else {
			return Direction.UP;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}

enum Direction {
	LEFT, RIGHT, DOWN, UP;
}

enum CellType {
	EMPTY('.'), WALL('#'), UNKNOWN('?'), START('T'), CONTROL_ROOM('C');

	private static final Map<Character, CellType> BY_CHARACTER = new HashMap<>();

	static {
		for (CellType e : values()) {
			BY_CHARACTER.put(e.character, e);
		}
	}

	public final char character;

	private CellType(char character) {
		this.character = character;
	}

	public static CellType valueOfChar(char character) {
		return BY_CHARACTER.get(character);
	}
}

class Cell {
	Position position;
	CellType type;
	
	public Cell(int x, int y, char type) {
		this.position = new Position(x, y);
		this.type = CellType.valueOfChar(type);
	}
	
	public Cell(Position position, char type) {
		this.position = new Position(position.x, position.y);
		this.type = CellType.valueOfChar(type);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}
}

class Labyrinth {
	Map<Cell, List<Cell>> adjacentCells;
	private int rows;
	private int columns;

	void addCell(Cell cell) {
		adjacentCells.putIfAbsent(cell, new ArrayList<>());
	}

	void removeCell(Cell cell) {
		adjacentCells.values().stream().forEach(e -> e.remove(cell));
		adjacentCells.remove(cell);
	}

	void addEdge(Cell cell1, Cell cell2) {
		adjacentCells.get(cell1).add(cell2);
		adjacentCells.get(cell2).add(cell1);
	}

	public Labyrinth(char[][] labyrinthAsCharArray, int rows, int columns) {
		this.adjacentCells = new HashMap<>();
		this.rows = rows;
		this.columns = columns;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (labyrinthAsCharArray[i][j] == '#') {
					continue;
				}
				Cell currentCell = new Cell(j, i, labyrinthAsCharArray[i][j]);
				addCell(currentCell);

				if (j < columns - 1 && labyrinthAsCharArray[i][j + 1] != '#') {
					Cell rightCell = new Cell(j + 1, i, labyrinthAsCharArray[i][j + 1]);
					addCell(rightCell);
					addEdge(currentCell, rightCell);
				}
				if (i < rows - 1 && labyrinthAsCharArray[i + 1][j] != '#') {
					Cell bottomCell = new Cell(j, i + 1, labyrinthAsCharArray[i + 1][j]);
					addCell(bottomCell);
					addEdge(currentCell, bottomCell);
				}
			}
		}
	}

	public void discoverNewCells(char[][] labyrinthAsCharArray, Direction direction, Position position) {
		List<Position> positionsToUpdate = new ArrayList<>();

		switch (direction) {
			case DOWN:
				if (position.y + 2 < rows) {
					positionsToUpdate.add(new Position(position.x, position.y + 2));
					if (position.x - 1 >= 0) {
						positionsToUpdate.add(new Position(position.x - 1, position.y + 2));
					}
					if (position.x - 2 >= 0) {
						positionsToUpdate.add(new Position(position.x - 2, position.y + 2));
					}
					if (position.x + 1 < columns) {
						positionsToUpdate.add(new Position(position.x + 1, position.y + 2));
					}
					if (position.x + 2 < columns) {
						positionsToUpdate.add(new Position(position.x + 2, position.y + 2));
					}
				}
				break;
			case LEFT:
				if (position.x - 2 >= 0) {
					positionsToUpdate.add(new Position(position.x - 2, position.y));
					// if possible
					if (position.y - 1 >= 0) {
						positionsToUpdate.add(new Position(position.x - 2, position.y - 1));
					}
					if (position.y - 2 >= 0) {
						positionsToUpdate.add(new Position(position.x - 2, position.y - 2));
					}
					if (position.y + 1 < rows) {
						positionsToUpdate.add(new Position(position.x - 2, position.y + 1));
					}
					if (position.y + 2 < rows) {
						positionsToUpdate.add(new Position(position.x - 2, position.y + 2));
					}
				}
				break;
			case RIGHT:
				if (position.x + 2 < columns) {
					positionsToUpdate.add(new Position(position.x + 2, position.y));
					if (position.y - 1 >= 0) {
						positionsToUpdate.add(new Position(position.x + 2, position.y - 1));
					}
					if (position.y - 2 >= 0) {
						positionsToUpdate.add(new Position(position.x + 2, position.y - 2));
					}
					if (position.y + 1 < rows) {
						positionsToUpdate.add(new Position(position.x + 2, position.y + 1));
					}
					if (position.y + 2 < rows) {
						positionsToUpdate.add(new Position(position.x + 2, position.y + 2));
					}
				}
				break;
			case UP:
				if (position.y - 2 >= 0) {
					positionsToUpdate.add(new Position(position.x, position.y - 2));
					if (position.x - 1 >= 0) {
						positionsToUpdate.add(new Position(position.x - 1, position.y - 2));
					}
					if (position.x - 2 >= 0) {
						positionsToUpdate.add(new Position(position.x - 2, position.y - 2));
					}
					if (position.x + 1 < columns) {
						positionsToUpdate.add(new Position(position.x + 1, position.y - 2));
					}
					if (position.x + 2 < columns) {
						positionsToUpdate.add(new Position(position.x + 2, position.y - 2));
					}
				}
				break;
			default:
				break;
		}

		for (Position positionToUpdate : positionsToUpdate) {
			Cell cellFromMap = getCellAt(positionToUpdate);
			CellType newCellType = CellType.valueOfChar(labyrinthAsCharArray[positionToUpdate.y][positionToUpdate.x]);

			if (newCellType.equals(CellType.WALL)) {
				removeCell(cellFromMap);
			} else {
				cellFromMap.type = newCellType;
				adjacentCells.values().stream().forEach(e -> {
					int index = e.indexOf(cellFromMap);
					if (index != -1) {
						Cell cell = e.get(index);
						cell.type = newCellType;
					}
				});
			}
		}
	}

	Cell getCellAt(Position position) {
		Set<Cell> cells = adjacentCells.keySet();

		for (Cell cell : cells) {
			if (cell.position.equals(position)) {
				return cell;
			}
		}

		return null;
	}

	List<Cell> getAdjacentCells(Cell cell) {
		return adjacentCells.get(cell);
	}

	Cell findNextCellLeadingTo(Position rootPosition, CellType type, boolean ignoreUnknownCases) {
		Cell root = new Cell(rootPosition.x, rootPosition.y, '.');
		Map<Cell, Cell> visited = new LinkedHashMap<>(); // visited cells mapped to their parent
		Queue<Cell> queue = new LinkedList<>();
		queue.add(root);
		visited.put(root, root);
		boolean cellFound = false;
		Cell result = null;

		while (!queue.isEmpty() && !cellFound) {
			Cell cell = queue.poll();
			for (Cell c : getAdjacentCells(cell)) {
				if (ignoreUnknownCases && c.type.equals(CellType.UNKNOWN)) {
					continue;
				}
				if (!visited.containsKey(c)) {
					visited.put(c, cell);
					queue.add(c);

					if (c.type.equals(type)) {
						cellFound = true;
						result = c;
						break;
					}
				}
			}
		}

		Cell parent = visited.get(result);
		while (!parent.equals(root)) {
			result = parent;
			parent = visited.get(result);
		}

		return result;
	}

	public int computeFastestWayTo(Position rootPosition, CellType type) {
		Cell root = new Cell(rootPosition.x, rootPosition.y, '?');
		Map<Cell, Cell> visited = new LinkedHashMap<>(); // visited cells mapped to their parent
		Queue<Cell> queue = new LinkedList<>();
		queue.add(root);
		visited.put(root, root);
		boolean cellFound = false;
		Cell result = null;

		while (!queue.isEmpty() && !cellFound) {
			Cell cell = queue.poll();
			for (Cell c : getAdjacentCells(cell)) {
				if (c.type.equals(CellType.UNKNOWN)) {
					continue;
				}
				if (!visited.containsKey(c)) {
					visited.put(c, cell);
					queue.add(c);

					if (c.type.equals(type)) {
						cellFound = true;
						result = c;
						break;
					}
				}
			}
		}

		if (cellFound) {
			int turnCount = 1;
			Cell parent = visited.get(result);
			while (!parent.equals(root)) {
				result = parent;
				parent = visited.get(result);
				turnCount++;
			}

			return turnCount;
		}

		return -1;
	}
}

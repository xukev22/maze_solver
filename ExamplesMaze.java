import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// handles the user interaction for a maze
class MazeSolver extends World {

  // cannot be final as maze will need to be modified to make new mazes
  private Maze maze;

  // game states must be dynamic in order to have an interactive game, cannot be
  // final
  private boolean bfsActive = false;
  private boolean dfsActive = false;
  private boolean manualActive = false;
  private boolean userWon = false;
  private int wrongMoves = 0;
  private int dfsWrongMoves = -1;
  private int bfsWrongMoves = -1;
  private boolean underConstruction = true;

  private boolean showVisited = true;
  private boolean showStartHeatMap = false;
  private boolean showExitHeatMap = false;
  private boolean showAlgorithmComparison = false;

  // constructs a game given a maze state
  MazeSolver(Maze maze) {
    this.maze = maze;
  }

  // visualizes the maze
  public WorldScene makeScene() {
    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(this.maze.render(this.userWon, this.showVisited, this.wrongMoves,
        this.showAlgorithmComparison, this.dfsWrongMoves, this.bfsWrongMoves), 500, 500);
    return w;
  }

  // EFFECT: updates the game state based on the key press
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      this.resetGameStatesForSearch();

      this.bfsActive = true;
      this.dfsActive = false;

      this.setUpMazeForSearch();

    }
    else if (key.equals("d")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      this.resetGameStatesForSearch();

      this.dfsActive = true;
      this.bfsActive = false;

      this.setUpMazeForSearch();
    }
    else if (key.equals("r")) {
      this.resetGameStatesForConstruction();

      this.bfsActive = false;
      this.dfsActive = false;

      // reset maze w/ random seed from 0-9999 and same bias
      this.maze = new Maze(this.maze, (int) (Math.random() * 10000));
    }
    else if (key.equals("0")) {
      this.resetGameStatesForConstruction();

      this.bfsActive = false;
      this.dfsActive = false;

      // reset maze w/ random seed from 0-9999 and no bias
      this.maze = new Maze(this.maze, (int) (Math.random() * 10000), 0);
    }
    else if (key.equals("1")) {
      this.resetGameStatesForConstruction();

      this.bfsActive = false;
      this.dfsActive = false;

      // reset maze w/ random seed from 0-9999 and horizontal bias
      this.maze = new Maze(this.maze, (int) (Math.random() * 10000), 1);
    }
    else if (key.equals("2")) {
      this.resetGameStatesForConstruction();

      this.bfsActive = false;
      this.dfsActive = false;

      // reset maze w/ random seed from 0-9999 and vertical bias
      this.maze = new Maze(this.maze, (int) (Math.random() * 10000), 2);
    }
    else if (key.equals("m")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      this.userWon = false;
      this.manualActive = true;
      this.wrongMoves = 0;

      // reset w/ default seed of 420
      this.maze = new Maze(this.maze);

      this.bfsActive = false;
      this.dfsActive = false;

      // go left (into wall but draw tile)
      this.maze.attemptMove("left");

      // prep drawing correct path
      this.maze.buildReferencesPath();
      // label the cells that are correct
      this.maze.labelCorrectCells();
    }
    else if (key.equals("left") || key.equals("right") || key.equals("up") || key.equals("down")) {

      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      if (this.manualActive) {
        // we can move (in manual mode)
        // receive the package state
        Pair<Boolean, Integer> attemptMoveReturn = this.maze.attemptMove(key);
        // obj1 is if we are done or not
        this.manualActive = attemptMoveReturn.getObj1();
        // obj2 is the amount of wrong moves
        this.wrongMoves += attemptMoveReturn.getObj2();
        if (!this.manualActive) {
          this.userWon = true;
        }
      }

    }
    else if (key.equals("v")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      // toggle view model of showing visited cells
      this.showVisited = !this.showVisited;

    }
    else if (key.equals("s")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      // disable exit heatmap toggle
      this.showExitHeatMap = false;

      if (this.showStartHeatMap) {
        // if start heatmap is already on, disable it
        this.maze.resetGradient();
      }
      else {
        // if start heatmap is not on, enable it
        this.maze.setStartGradient();
      }

      this.showStartHeatMap = !this.showStartHeatMap;
    }
    else if (key.equals("e")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      // disable start heatmap toggle
      this.showStartHeatMap = false;

      if (this.showExitHeatMap) {
        // if exit heatmap is already on, disable it
        this.maze.resetGradient();
      }
      else {
        // if exit heatmap is not on, enable it
        this.maze.setExitGradient();
      }

      this.showExitHeatMap = !this.showExitHeatMap;
    }
    else if (key.equals("p")) {
      if (this.underConstruction) {
        // can only rebuild new mazes when under construction
        return;
      }

      // toggle view model of showing algorithm comparison
      this.showAlgorithmComparison = !this.showAlgorithmComparison;
    }
    else if (key.equals("u")) {
      if (this.underConstruction) {
        // if we are under construction, we can skip construction with "u"
        this.maze = new Maze(this.maze);
        this.underConstruction = false;
      }
    }
    else {
      // do nothing, invalid key press
      return;
    }
  }

  // EFFECT: resets the game states for construction
  void resetGameStatesForConstruction() {
    this.userWon = false;
    this.manualActive = false;
    this.wrongMoves = 0;

    this.dfsWrongMoves = -1;
    this.bfsWrongMoves = -1;

    this.underConstruction = true;
  }

  // EFFECT: set up the maze for searching
  void setUpMazeForSearch() {
    // initialize auto search algorithm
    this.maze.initMazeAuxilaries();
    // label the cells that are correct
    this.maze.labelCorrectCells();
  }

  // EFFECT: resets the game states for searching
  void resetGameStatesForSearch() {
    this.userWon = false;
    this.manualActive = false;
    this.wrongMoves = 0;

    // reset w/ previous seed
    this.maze = new Maze(this.maze);
  }

  // EFFECT: updates the game state based on the previous game state
  public void onTick() {
    if (this.bfsActive) {
      // call BFS on maze
      Pair<Boolean, Integer> updateSearchReturn = this.maze.updateSearch(true);
      this.bfsActive = updateSearchReturn.getObj1();
      this.wrongMoves += updateSearchReturn.getObj2();

      if (!this.bfsActive) {
        this.bfsWrongMoves = this.wrongMoves;
      }
    }
    else if (this.dfsActive) {
      // call DFS on maze
      Pair<Boolean, Integer> updateSearchReturn = this.maze.updateSearch(false);
      this.dfsActive = updateSearchReturn.getObj1();
      this.wrongMoves += updateSearchReturn.getObj2();

      if (!this.dfsActive) {
        this.dfsWrongMoves = this.wrongMoves;
      }
    }
    else if (this.underConstruction) {
      // "remove" an edge
      this.underConstruction = this.maze.removeWalls();
    }

  }

}

// represents packaged info of two types
class Pair<X, Y> {
  private final X obj1;
  private final Y obj2;

  Pair(X obj1, Y obj2) {
    this.obj1 = obj1;
    this.obj2 = obj2;
  }

  // need getters to access fields as their sole purpose is to package info
  X getObj1() {
    return this.obj1;
  }

  // need getters to access fields as their sole purpose is to package info
  Y getObj2() {
    return this.obj2;
  }
}

// represents either a border cell or a non-border cell
interface ICell {
  // determines if two cells are linked through an edge
  boolean isLinked(ICell other);

  // determines if this ICell is linked through an edge to another cell
  boolean isLinkedHelp(Cell other);

  // visualizes a row of cells
  WorldImage renderRow(boolean showVisited, int maxSteps);

  // visualizes a grid of cells
  WorldImage render(boolean showVisited, int maxSteps);

  // returns the cell equivalent of this ICell
  Cell asCell();

  // returns the highest step count cell in this row
  int findMaxStepsInRow(int prevMax);

  // returns the highest step count cell in this grid
  int findMaxSteps(int prevMax);
}

// represents the outside of a maze
class BorderCell implements ICell {

  // a border cell is never linked through an edge to any other cell
  public boolean isLinked(ICell other) {
    return false;
  }

  // a border cell is never linked through an edge to any other cell
  public boolean isLinkedHelp(Cell other) {
    return false;
  }

  // a border cell has no image
  public WorldImage renderRow(boolean showVisited, int maxSteps) {
    return new EmptyImage();
  }

  // a border cell has no image
  public WorldImage render(boolean showVisited, int maxSteps) {
    return new EmptyImage();
  }

  // cannot call asCell on border cell
  public Cell asCell() {
    throw new RuntimeException("Cannot call asCell on border cell!");
  }

  // returns the accumulated max step count cell in this row
  public int findMaxStepsInRow(int prevMax) {
    return prevMax;
  }

  // returns the accumulated max step count cell in this grid
  public int findMaxSteps(int prevMax) {
    return prevMax;
  }

}

// represents a cell in the maze
class Cell implements ICell {
  private boolean startNodeIndicator = false;
  private boolean endNodeIndicator = false;

  // cannot be final as it will be mutated within the maze to represent different
  // render states
  private boolean visited;
  private boolean solution;
  private boolean containsPlayer;
  private boolean correctPath;
  private boolean displayGradient;
  private int stepsFromStart;

  // private final int id;

  // all of these cannot be final because to initialize the links in a maze the
  // edge must be modified to another node
  private Edge top;
  private Edge right;
  private Edge bottom;
  private Edge left;

  // convenience constructor for Cell
  Cell() {
    this.top = new Edge();
    this.bottom = new Edge();
    this.left = new Edge();
    this.right = new Edge();
  }

  // constructs a full cell
  Cell(Edge top, Edge right, Edge bottom, Edge left) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }

  // EFFECT: sets this node's edge at the given direction to the given edge
  // 0 represents top, 1 represents right, 2 represents bottom, 3 represents left
  void setEdge(Edge e, int direction) {
    if (direction == 0) {
      this.top = e;
    }
    else if (direction == 1) {
      this.right = e;
    }
    else if (direction == 2) {
      this.bottom = e;
    }
    else if (direction == 3) {
      this.left = e;
    }
    else {
      throw new RuntimeException("Direction must be between 0 and 3 inclusive!");
    }
  }

  // checks if this cell is linked through an edge to the other ICell
  public boolean isLinked(ICell other) {
    return other.isLinkedHelp(this);
  }

  // this cell is linked through an edge to another cell if they share a
  // corresponding edge
  public boolean isLinkedHelp(Cell other) {
    return this.left == other.right || this.top == other.bottom || this.right == other.left
        || this.bottom == other.top;
  }

  // EFFECT: mutates the given list of edges and adds each edge based on the given
  // direction: 2 means bottom, 1 means right
  void addEdges(List<Edge> worklist, int direction) {
    if (direction > 2 || direction < 1) {
      throw new RuntimeException("addEdges needs a direction between 1 and 2 inclusive!");
    }
    if (direction == 1) {
      worklist.add(this.right);
    }

    if (direction == 2) {
      worklist.add(this.bottom);
    }

  }

  // visualizes the grid of cells
  public WorldImage render(boolean showVisited, int maxSteps) {
    return new AboveImage(this.renderRow(showVisited, maxSteps),
        this.bottom.getNode1().render(showVisited, maxSteps));
  }

  // visualizes a row of cells based on the indicators
  public WorldImage renderRow(boolean showVisited, int maxSteps) {
    if (this.displayGradient) {
      double scaledTo1 = (double) this.stepsFromStart / (double) (maxSteps);

      int red = (int) (255 * (1 - scaledTo1));
      int blue = (int) (255 * scaledTo1);

      return new BesideImage(
          this.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(red, 0, blue))),
          this.right.getNode2().renderRow(showVisited, maxSteps));
    }
    else if (this.solution) {
      return new BesideImage(
          this.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
          this.right.getNode2().renderRow(showVisited, maxSteps));
    }
    else if (this.containsPlayer) {
      return new BesideImage(
          this.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176))),
          this.right.getNode2().renderRow(showVisited, maxSteps));
    }
    else if (this.visited && showVisited) {
      return new BesideImage(
          this.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233))),
          this.right.getNode2().renderRow(showVisited, maxSteps));
    }
    else {
      return new BesideImage(
          this.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
          this.right.getNode2().renderRow(showVisited, maxSteps));
    }

  }

  // find the max step count cell in this grid
  public int findMaxSteps(int prevMax) {
    int maxStepsInRow = this.findMaxStepsInRow(prevMax);
    if (prevMax < maxStepsInRow) {
      return this.bottom.getNode1().findMaxSteps(maxStepsInRow);
    }
    else {
      return this.bottom.getNode1().findMaxSteps(prevMax);
    }
  }

  // find the max step count cell in this row
  public int findMaxStepsInRow(int prevMax) {
    if (prevMax < this.stepsFromStart) {
      return this.right.getNode2().findMaxStepsInRow(this.stepsFromStart);
    }
    else {
      return this.right.getNode2().findMaxStepsInRow(prevMax);
    }
  }

  // visualizes a single cell with the borders
  WorldImage drawCell(WorldImage soFar) {
    if (this.startNodeIndicator) {
      soFar = new OverlayImage(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
          soFar);
    }

    if (this.endNodeIndicator) {
      soFar = new OverlayImage(
          new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)), soFar);
    }

    return new OverlayOffsetImage(this.left.render(true), 4.5, 0,
        new OverlayOffsetImage(this.bottom.render(false), 0, -4.5,
            new OverlayOffsetImage(this.top.render(false), 0, 4.5,
                new OverlayOffsetImage(this.right.render(true), -4.5, 0, soFar))));
  }

  // returns the list of connected neighbors for this cell
  ArrayList<Cell> addConnectedNeighbors() {
    ArrayList<Cell> returnList = new ArrayList<Cell>();
    returnList.addAll(this.top.addIfPossible(0));
    returnList.addAll(this.right.addIfPossible(1));
    returnList.addAll(this.bottom.addIfPossible(2));
    returnList.addAll(this.left.addIfPossible(3));
    return returnList;
  }

  // returns this cell's reference
  public Cell asCell() {
    return this;
  }

  // EFFECT: sets the visited field to the given boolean, as needed for animation
  void setVisited(boolean visited) {
    this.visited = visited;
  }

  // EFFECT: sets the solution field to the given boolean, as needed for animation
  void setSolution(boolean solution) {
    this.solution = solution;
  }

  // EFFECT: recursively back tracks the hashmap and sets each cell's solution
  // field to true
  void drawPathBack(HashMap<Cell, Cell> references) {
    this.setSolution(true);
    if (references.get(this) == null) {
      return;
    }
    references.get(this).drawPathBack(references);
  }

  // EFFECT: sets the correct field to the given boolean, as needed for checking
  // if the algorithm or player is on the correct path
  void setCorrectPath(boolean correctPath) {
    this.correctPath = correctPath;
  }

  // return's the cell at the given direction, if it is a valid move
  public Cell moveIfCan(String direction) {
    if (direction.equals("up")) {
      return this.top.changeIfCan(direction, this);
    }
    else if (direction.equals("right")) {
      return this.right.changeIfCan(direction, this);
    }
    else if (direction.equals("down")) {
      return this.bottom.changeIfCan(direction, this);
    }
    else if (direction.equals("left")) {
      return this.left.changeIfCan(direction, this);
    }
    else {
      throw new RuntimeException("Invalid direction given to moveIfCan");
    }
  }

  // EFFECT: sets the containsPlayer field to the given boolean, as needed for
  // checking if a player is on this cell or not
  void setPlayerIndicator(boolean indicator) {
    this.containsPlayer = indicator;
  }

  // returns whether or not this cell is an incorrect move, and a unique one
  // (hasn't been visited)
  boolean uniqueIncorrectMove() {
    return !this.correctPath && !this.visited;
  }

  // EFFECT: labels the correct path for each cell by recursively backtracking
  // through the given hashmap
  void labelCorrectPath(HashMap<Cell, Cell> finalReferences) {
    this.setCorrectPath(true);
    if (finalReferences.get(this) == null) {
      return;
    }
    finalReferences.get(this).labelCorrectPath(finalReferences);
  }

  // EFFECT: traverses through the graph, either labeling each cell with a
  // stepFromStart count or resetting the gradient display field
  void traverse(int stepsFromStart, ArrayList<Cell> alreadySeen, boolean reset) {
    if (alreadySeen.contains(this)) {
      return;
    }

    alreadySeen.add(this);

    if (reset) {
      this.resetGradient();
    }
    else {
      this.setGradient(stepsFromStart);
    }

    // iterates through the cells in the connected neighbors and recursively
    // traverses each of those
    for (Cell cell : this.addConnectedNeighbors()) {
      cell.traverse(stepsFromStart + 1, alreadySeen, reset);
    }
  }

  // EFFECT: resets the gradient display and step count from the start of this
  // cell
  void resetGradient() {
    this.displayGradient = false;
    this.stepsFromStart = 0;
  }

  // EFFECT: resets the gradient display and step count from the start of this
  // cell
  void setGradient(int stepsFromStart) {
    this.displayGradient = true;
    this.stepsFromStart = stepsFromStart;

  }

  // EFFECT: sets the start field of this cell to true, as needed to display the
  // start and end node
  void setStart() {
    this.startNodeIndicator = true;
  }

  // EFFECT: sets the endNode of this cell to true, as needed to display the
  // start and end node
  void setEnd() {
    this.endNodeIndicator = true;
  }

}

// A function object that compares two edges weights
class CompareEdges implements Comparator<Edge> {

  // compares two doubles
  public int compare(Edge o1, Edge o2) {
    if (o1.getWeight() > o2.getWeight()) {
      return 1;
    }
    else if (o1.getWeight() < o2.getWeight()) {
      return -1;
    }
    else {
      return 0;
    }
  }

}

// represents a connection between cells in a maze
class Edge {

  private final double weight;

  // cannot be final as needs to be changed to indicate the minimum spanning
  // tree's pathways
  private boolean connected;

  private final ICell node1;
  private final ICell node2;

  // Convenience constructor for edge, uses two nodes and assigns dummy (-1)
  // weight
  Edge(ICell node1, ICell node2) {
    this(node1, node2, -1);
  }

  // returns a new cell if the other cell is connected to this edge, otherwise
  // returns the cell it was given
  public Cell changeIfCan(String direction, Cell base) {
    if (this.connected) {
      if (direction.equals("up")) {
        return this.node2.asCell();
      }
      else if (direction.equals("right")) {
        return this.node2.asCell();
      }
      else if (direction.equals("down")) {
        return this.node1.asCell();
      }
      else if (direction.equals("left")) {
        return this.node1.asCell();
      }
    }

    return base;
  }

  // adds the cell to a list of cells if it can, meaning if this edge is a
  // connected edge
  List<Cell> addIfPossible(int direction) {
    if (direction < 0 || direction > 3) {
      throw new RuntimeException("Direction must be between 0 and 3 inclusive");
    }
    ArrayList<Cell> returnList = new ArrayList<>();
    if (this.connected) {
      if (direction == 0) {
        returnList.add(this.node2.asCell());
      }
      else if (direction == 1) {
        returnList.add(this.node2.asCell());
      }
      else if (direction == 2) {
        returnList.add(this.node1.asCell());
      }
      else if (direction == 3) {
        returnList.add(this.node1.asCell());
      }
    }

    return returnList;
  }

  // constructs an edge where nodes are not connected
  Edge(ICell node1, ICell node2, double weight) {
    this.weight = weight;
    this.connected = false;
    this.node1 = node1;
    this.node2 = node2;
  }

  // convenience constructor for empty edge
  Edge() {
    this(new BorderCell(), new BorderCell());
  }

  // convenience constructor for a given edge weight
  Edge(double weight) {
    this(null, null, weight);
  }

  // renders this edge by overlaying a wall, depending on if this is a connected
  // edge and if it should be vertical/horizontal
  WorldImage render(boolean vertical) {
    if (!this.connected) {
      if (vertical) {
        return new RectangleImage(1, 10, OutlineMode.SOLID, Color.black);
      }
      else {
        return new RectangleImage(10, 1, OutlineMode.SOLID, Color.black);
      }
    }
    else {
      return new EmptyImage();
    }

  }

  // returns the weight of this edge:
  // necessary to access the weight so the comparator can sort the
  // weights
  public double getWeight() {
    return this.weight;
  }

  // Returns node1 of this edge, as needed in the find() method for creating the
  // minimum spanning tree
  ICell getNode1() {
    return this.node1;
  }

  // Returns node2 of this edge, as needed in the find() method for creating the
  // minimum spanning tree
  ICell getNode2() {
    return this.node2;
  }

  // EFFECT: sets connected to true, needed to indicate the minimum spanning
  // tree's pathways
  void setTrue() {
    this.connected = true;
  }
}

// represents a minimum spanning tree of connected cells
class Maze {
  // cannot be final as needs to mutate for animation
  private int width;
  private int height;
  private int biasMode;
  // BIAS MODES:
  // 0, no bias
  // 1, horizontal bias
  // 2, vertical bias

  private Cell manualLocation;
  // cannot be final as player will be moving around the maze

  // start of a maze
  private final Cell startNode;
  // end of a maze
  private final Cell endNode;
  // random generation, first maze default seed is 420 if no seed is specified
  private final int seed;

  private final List<Edge> connectedEdges;

  private ArrayList<Cell> worklist;
  private ArrayList<Cell> alreadySeen;

  private HashMap<Cell, Cell> references;
  private HashMap<Cell, Cell> finalReferences;

  // convenience constructor for given width and height, uses default seed 420,
  // animates, and no bias
  Maze(int width, int height) {
    this(width, height, 420, true, 0);
  }

  // convenience constructor for given width, height, and biasMode, uses default
  // seed 420, and animates
  Maze(int width, int height, int biasMode) {
    this(width, height, 420, true, biasMode);
  }

  // EFFECT: traverses through the nodes and labels each cell with the amount of
  // steps from the exit and changing their gradient display value
  void setExitGradient() {
    this.endNode.traverse(0, new ArrayList<Cell>(), false);
  }

  // EFFECT: traverses through the nodes and alters their gradient display value
  void resetGradient() {
    this.startNode.traverse(0, new ArrayList<Cell>(), true);
  }

  // EFFECT: traverses through the nodes and labels each cell with the amount of
  // steps from the start and changing their gradient display value
  void setStartGradient() {
    this.startNode.traverse(0, new ArrayList<Cell>(), false);
  }

  // EFFECT: sets this edge to true and alters the connectedEdges list
  // a given amount of times based on the size
  // returns whether we should keep removing a wall (animating)
  boolean removeWalls() {
    if (this.connectedEdges.size() == 0) {
      return false;
    }
    else {
      if (this.connectedEdges.size() > 1000) {
        this.removeNWalls(100);
      }
      else if (this.connectedEdges.size() > 100) {
        this.removeNWalls(10);
      }
      else if (this.connectedEdges.size() > 10) {
        this.removeNWalls(3);
      }
      else {
        this.removeNWalls(1);
      }
      return true;
    }
  }

  // EFFECT: removes the given walls based on the given amount of iterations
  void removeNWalls(int iterations) {
    // in each iteration removes a wall
    for (int iteration = 0; iteration < iterations; iteration += 1) {
      Edge e = this.connectedEdges.remove(this.connectedEdges.size() - 1);
      e.setTrue();
    }
  }

  // EFFECT: labels the correct cells in this path by backtracking through the
  // final references, starting at the end node
  void labelCorrectCells() {
    this.endNode.labelCorrectPath(this.finalReferences);
  }

  // EFFECT: attempts to move the player based on the given direction
  // also returns packaged info on whether or not to keep going, and the amount of
  // wrong moves this action made
  Pair<Boolean, Integer> attemptMove(String direction) {
    int wrongMoveCount = 0;
    this.manualLocation.setPlayerIndicator(false);
    this.manualLocation = this.manualLocation.moveIfCan(direction);

    if (this.manualLocation.uniqueIncorrectMove()) {
      wrongMoveCount = 1;
    }

    this.manualLocation.setPlayerIndicator(true);
    this.manualLocation.setVisited(true);

    if (this.manualLocation == this.endNode) {
      this.endNode.drawPathBack(this.finalReferences);
      return new Pair<Boolean, Integer>(false, wrongMoveCount);
    }
    else {
      return new Pair<Boolean, Integer>(true, wrongMoveCount);
    }
  }

  // EFFECT: creates the final references list (used to calculate the correct
  // path)
  void buildReferencesPath() {
    this.initMazeAuxilaries();

    // INVARIANT:
    // the worklist is a finite size, and we either decrease the size of the
    // worklist or add it to already seen, meaning the size of the
    // list ultimately decreases in either case

    // builds the final references map of the correct path
    while (this.worklist.size() != 0) {
      Cell next = this.worklist.remove(0);
      if (next == this.endNode) {
        return;
      }
      else {
        // iterates through the cells in the connected neighbors and recursively
        // adds those to the worklist (if unique)
        // also places them in the hashmap of final references
        for (Cell neighboringCell : next.addConnectedNeighbors()) {
          // add to front if unique
          if (!this.alreadySeen.contains(neighboringCell)) {
            this.worklist.add(neighboringCell);
            this.finalReferences.put(neighboringCell, next);
          }
        }
        this.alreadySeen.add(next);
      }
    }

  }

  // constructs a new maze given a width, height, seed, animation toggle, and bias
  // mode
  Maze(int width, int height, int seed, boolean animateConstruction, int biasMode) {
    this.connectedEdges = new ArrayList<Edge>();
    this.seed = seed;

    Random random = new Random(this.seed);

    if (biasMode < 0 || biasMode > 2) {
      throw new IllegalArgumentException("Bias mode must be 0, 1 or 2");
    }
    // a maze is non-sensical when a 1x1 or smaller
    if (width < 2 || height < 2) {
      throw new IllegalArgumentException("Width and height need to be 2 or more!");
    }

    this.biasMode = biasMode;

    this.width = width;
    this.height = height;

    // outer list is rows, inner list is cols
    // 0 represents top row, increasing index goes down
    // 0 represents left col, increasing index goes right
    ArrayList<ArrayList<Cell>> nodeGrid = new ArrayList<>();

    // initializes the rows in the 2d array list
    for (int rowIndex = 0; rowIndex < height; rowIndex += 1) {
      nodeGrid.add(new ArrayList<>());
    }

    // places nodes in all the indexes of the grid of nodes
    for (int rowIndex = 0; rowIndex < height; rowIndex += 1) {
      // places nodes in all the columns of this grid row
      for (int colIndex = 0; colIndex < width; colIndex += 1) {
        nodeGrid.get(rowIndex).add(new Cell());

      }
    }

    // start node will be top left
    this.startNode = nodeGrid.get(0).get(0);
    this.startNode.setStart();

    // player starts at top left
    this.manualLocation = nodeGrid.get(0).get(0);

    // end node will be bottom right
    this.endNode = nodeGrid.get(height - 1).get(width - 1);
    this.endNode.setEnd();

    this.linkGrid(nodeGrid, random, biasMode);
    this.createMinimumSpanning(nodeGrid, animateConstruction);

  }

  // constructs the same maze but without the animation
  public Maze(Maze maze) {
    this(maze.width, maze.height, maze.seed, false, maze.biasMode);
  }

  // constructs a new random maze with the animation
  public Maze(Maze maze, int seed) {
    this(maze.width, maze.height, seed, true, maze.biasMode);
  }

  // constructs the same maze with the seed and bias mode, with the animation
  public Maze(Maze maze, int seed, int bias) {
    this(maze.width, maze.height, seed, true, bias);
  }

  // EFFECT: initializes the maze auxiliaries in order to set up search and manual
  // traversals
  public void initMazeAuxilaries() {
    this.worklist = new ArrayList<>();
    this.worklist.add(this.startNode);
    this.alreadySeen = new ArrayList<>();
    this.references = new HashMap<>();
    this.finalReferences = new HashMap<>();
  }

  // EFFECT: searches for the next cell based on the search mode
  // also returns packaged info on whether or not to keep going, and the amount of
  // wrong moves this action made
  public Pair<Boolean, Integer> updateSearch(boolean bfs) {
    int wrongMoveCount = 0;

    if (this.worklist.size() == 0) {
      return new Pair<Boolean, Integer>(false, 0);
    }
    else {
      Cell next = this.worklist.remove(0);
      next.setVisited(true);
      if (next == this.endNode) {
        // next is the target node

        this.endNode.drawPathBack(this.references);
        return new Pair<Boolean, Integer>(false, wrongMoveCount);
      }
      else {
        // iterates through the cells in the connected neighbors and recursively
        // adds them to the work list if unique
        for (Cell neighboringCell : next.addConnectedNeighbors()) {
          // add to front if unique
          if (!this.alreadySeen.contains(neighboringCell)) {

            if (neighboringCell.uniqueIncorrectMove()) {
              wrongMoveCount += 1;
            }

            if (bfs) {
              this.worklist.add(neighboringCell);
            }
            else {
              this.worklist.add(0, neighboringCell);
            }

            this.references.put(neighboringCell, next);
          }
        }
        this.alreadySeen.add(next);
        return new Pair<Boolean, Integer>(true, wrongMoveCount);
      }
    }
  }

  // visualize the maze beginning at the start node
  WorldImage render(boolean userWon, boolean showVisited, int wrongMoves,
      boolean showAlgorithmComparison, int dfsWrongMoves, int bfsWrongMoves) {
    WorldImage baseImage = new AboveImage(
        new TextImage("Wrong Moves: " + wrongMoves, 20, Color.black),
        this.startNode.render(showVisited, this.startNode.findMaxSteps(-1)));

    if (showAlgorithmComparison) {
      WorldImage aboveImage = new EmptyImage();
      if (dfsWrongMoves == -1 || bfsWrongMoves == -1) {
        aboveImage = new TextImage("Finish DFS and BFS to see performance comparison!", 20,
            Color.black);
      }
      else if (dfsWrongMoves < bfsWrongMoves) {
        aboveImage = new TextImage("DFS had " + (bfsWrongMoves - dfsWrongMoves) + " fewer moves",
            20, Color.black);
      }
      else if (dfsWrongMoves == bfsWrongMoves) {
        aboveImage = new TextImage("DFS and BFS had the same number of moves", 20, Color.black);
      }
      else {
        aboveImage = new TextImage("BFS had " + (dfsWrongMoves - bfsWrongMoves) + " fewer moves",
            20, Color.black);
      }

      baseImage = new AboveImage(aboveImage, baseImage);
    }
    if (userWon) {
      return new AboveImage(
          new TextImage("Congrats you won! Press r (or any other mode toggle) to reset.", 20,
              Color.black),
          baseImage);
    }
    return baseImage;

  }

  // Returns the representative of a tree by recursively traversing the
  // representatives hashmap
  ICell find(HashMap<ICell, ICell> representatives, ICell node) {
    if (representatives.containsKey(node)) {
      if (representatives.get(node) == node) {
        return node;
      }
      else {
        return this.find(representatives, representatives.get(node));
      }
    }
    else {
      throw new RuntimeException("Node is not here");
    }

  }

  // EFFECT: Unions two trees together, by linking one representative to the
  // other's representative
  void union(HashMap<ICell, ICell> representatives, ICell cell1, ICell cell2) {
    representatives.put(this.find(representatives, cell1), this.find(representatives, cell2));
  }

  // EFFECT: creates a minimum spanning tree that connects every cell
  // they will be "connected" if they share an edge with its connected field as
  // "true"
  void createMinimumSpanning(ArrayList<ArrayList<Cell>> nodeGrid, boolean animateConstruction) {

    List<Edge> worklist = this.addUniqueEdges(nodeGrid, nodeGrid.size(), nodeGrid.get(0).size());

    HashMap<ICell, ICell> representatives = new HashMap<>();

    // sets every node to itself in the representatives hashmap
    for (int rowIndex = 0; rowIndex < nodeGrid.size(); rowIndex += 1) {
      // in this row sets every node to itself in the representatives hashmap
      for (int colIndex = 0; colIndex < nodeGrid.get(0).size(); colIndex += 1) {
        representatives.put(nodeGrid.get(rowIndex).get(colIndex),
            nodeGrid.get(rowIndex).get(colIndex));
      }
    }

    worklist.sort(new CompareEdges());

    // starts off empty
    List<Edge> edgesInTree = new ArrayList<>();

    // INVARIANT:
    // the worklist is a finite size, and we either decrease the size of the
    // worklist or make a representative point to another representative, decreasing
    // the amount of unique representatives (which is also finite), which after
    // enough iterations causes the find case to trigger, meaning the size of the
    // list ultimately decreases in either case

    // adds the edges that are relevant to forming a minimum spanning tree
    while (worklist.size() > 1) {
      Edge e = worklist.get(0);
      if (this.find(representatives, e.getNode1()) == this.find(representatives, e.getNode2())) {
        worklist.remove(0);
      }
      else {
        edgesInTree.add(e);
        this.union(representatives, this.find(representatives, e.getNode1()),
            this.find(representatives, e.getNode2()));
      }
    }

    // iterates through all the edges in the list and sets their connected field to
    // true
    for (Edge e : edgesInTree) {
      if (animateConstruction) {
        this.connectedEdges.add(e);
      }
      else {
        e.setTrue();
      }

    }

  }

  // EFFECT: adds edges from the nodeGrid to the worklist such that every edge is
  // added once
  ArrayList<Edge> addUniqueEdges(ArrayList<ArrayList<Cell>> nodeGrid, int rows, int cols) {
    ArrayList<Edge> returnList = new ArrayList<>();

    // iterates through the grid and adds unique edges, if possible
    for (int rowIndex = 0; rowIndex < rows; rowIndex += 1) {
      // adds unique edges in this row, if possible
      for (int colIndex = 0; colIndex < cols; colIndex += 1) {
        if (rowIndex == rows - 1 && colIndex == cols - 1) {
          // dont need to do anything when bottom right
        }
        else if (rowIndex == rows - 1) {
          // on the bottom row
          nodeGrid.get(rowIndex).get(colIndex).addEdges(returnList, 1);
        }
        else if (colIndex == cols - 1) {
          // on right col
          nodeGrid.get(rowIndex).get(colIndex).addEdges(returnList, 2);
        }
        else {
          // not on bottom or right edge
          nodeGrid.get(rowIndex).get(colIndex).addEdges(returnList, 1);
          nodeGrid.get(rowIndex).get(colIndex).addEdges(returnList, 2);
        }
      }
    }

    return returnList;
  }

  // EFFECT: links all the nodes in the grid with edges
  void linkGrid(ArrayList<ArrayList<Cell>> nodeGrid, Random seed, int biasMode) {

    // iterates through the grid and links nodes to their neighbors, if possible
    for (int rowIndex = 0; rowIndex < nodeGrid.size(); rowIndex += 1) {
      // links nodes in this row to their neighbors, if possible
      for (int colIndex = 0; colIndex < nodeGrid.get(0).size(); colIndex += 1) {
        Cell thisNode = nodeGrid.get(rowIndex).get(colIndex);

        double weight = seed.nextDouble();

        if (rowIndex == nodeGrid.size() - 1 && colIndex == nodeGrid.get(0).size() - 1) {
          // dont need to do anything when bottom right
        }
        else if (rowIndex == nodeGrid.size() - 1) {
          // on the bottom row
          if (biasMode == 1) {
            // horizontal bias
            weight /= 4.0;
          }
          this.setLink(thisNode, nodeGrid.get(rowIndex).get(colIndex + 1), 1, weight);
        }
        else if (colIndex == nodeGrid.get(0).size() - 1) {
          // on the right col
          if (biasMode == 2) {
            // vertical bias
            weight /= 4.0;
          }
          this.setLink(nodeGrid.get(rowIndex + 1).get(colIndex), thisNode, 0, weight);
        }
        else {

          double horWeight = weight;
          double verWeight = weight;

          if (biasMode == 1) {
            // horizontal bias

            horWeight = weight / 4.0;
          }
          else if (biasMode == 2) {
            // vertical bias
            verWeight = weight / 4.0;
          }
          else {
            horWeight = weight;
            verWeight = weight;
          }

          // not on bottom or right edge
          this.setLink(thisNode, nodeGrid.get(rowIndex).get(colIndex + 1), 1, horWeight);
          this.setLink(nodeGrid.get(rowIndex + 1).get(colIndex), thisNode, 0, verWeight);
        }
      }
    }

  }

  // EFFECT: sets the two nodes to each other by making an edge and assigning it
  // to each node, the constructed edge has random weight, and connected is false
  // a direction of 0 means top and bottom, a direction of 1 means left and right
  void setLink(Cell from, Cell to, int direction, double weight) {
    if (direction < 0 || direction > 1) {
      throw new RuntimeException("Direction must be between 0 and 1 inclusive!");
    }

    Edge e = new Edge(from, to, weight);
    from.setEdge(e, direction);
    to.setEdge(e, direction + 2);
  }
}

class ExamplesMaze {

  MazeSolver game;
  MazeSolver gameHorizontal;
  MazeSolver gameVertical;

  MazeSolver gameTiny;
  MazeSolver gameTinyCopy;

  Maze maze1;
  Maze maze1Copy;

  Maze mazeTiny;

  Cell cell1;
  Cell cell2;
  Cell cell1Copy;

  Edge edge1;

  HashMap<ICell, ICell> rep;
  Cell cellH1;
  Cell cellH2;
  Cell cellH3;
  Cell cellH4;

  BorderCell borderCell1;

  // EFFECT: initialize conditions
  void initConds() {

    // initialize cells
    this.cell1 = new Cell();
    this.cell1Copy = new Cell();
    this.cell2 = new Cell();
    this.edge1 = new Edge(this.cell1, this.cell2);

    // initialize a border cell
    this.borderCell1 = new BorderCell();

    this.maze1 = new Maze(6, 9);
    this.maze1Copy = new Maze(6, 9);

    this.mazeTiny = new Maze(2, 2);

    this.game = new MazeSolver(new Maze(100, 60));
    this.gameHorizontal = new MazeSolver(new Maze(40, 25, 1));
    this.gameVertical = new MazeSolver(new Maze(40, 25, 2));
    this.gameTiny = new MazeSolver(new Maze(2, 2));
    this.gameTinyCopy = new MazeSolver(new Maze(2, 2));

    // initialize representatives
    this.cellH1 = new Cell(new Edge(1), new Edge(2), new Edge(3), new Edge(4));
    this.cellH2 = new Cell(new Edge(4), new Edge(1), new Edge(2), new Edge(3));
    this.cellH3 = new Cell(new Edge(3), new Edge(4), new Edge(1), new Edge(2));
    this.cellH4 = new Cell(new Edge(2), new Edge(3), new Edge(4), new Edge(1));

    this.rep = new HashMap<>();
    this.rep.put(this.cellH1, this.cellH1);
    this.rep.put(this.cellH2, this.cellH1);
    this.rep.put(this.cellH3, this.cellH2);
    this.rep.put(this.cellH4, this.cellH4);

  }

  void testIsLinked(Tester t) {
    this.initConds();

    // any border cell is never connected to any cell
    t.checkExpect(new BorderCell().isLinked(this.cell1), false);
    t.checkExpect(new BorderCell().isLinkedHelp(this.cell1), false);
    t.checkExpect(this.cell1.isLinked(new BorderCell()), false);
    t.checkExpect(borderCell1.isLinked(new BorderCell()), false);
    // test that border cells are never linked to cells with filled edges
    t.checkExpect(borderCell1.isLinkedHelp(cellH1), false);

    // cell is not connected to itself
    t.checkExpect(this.cell1.isLinked(this.cell1), false);
    t.checkExpect(this.cell1.isLinkedHelp(this.cell1), false);

    // cell is connected for right and left
    this.cell1.setEdge(this.edge1, 1);
    this.cell2.setEdge(this.edge1, 3);
    // check that order doesnt matter
    t.checkExpect(this.cell1.isLinked(this.cell2), true);
    t.checkExpect(this.cell2.isLinked(this.cell1), true);
    // check helper
    t.checkExpect(this.cell1.isLinkedHelp(this.cell2), true);
    t.checkExpect(this.cell2.isLinkedHelp(this.cell1), true);

    this.initConds();
    // cell is connected for top and bottom
    this.cell1.setEdge(this.edge1, 0);
    this.cell2.setEdge(this.edge1, 2);
    // check that order doesnt matter
    t.checkExpect(this.cell1.isLinked(this.cell2), true);
    t.checkExpect(this.cell2.isLinked(this.cell1), true);
    // check helper
    t.checkExpect(this.cell1.isLinkedHelp(this.cell2), true);
    t.checkExpect(this.cell2.isLinkedHelp(this.cell1), true);

    this.initConds();
    // cell is not connected for top and left
    this.cell1.setEdge(this.edge1, 1);
    this.cell2.setEdge(this.edge1, 2);
    // check that order doesnt matter
    t.checkExpect(this.cell1.isLinked(this.cell2), false);
    t.checkExpect(this.cell2.isLinked(this.cell1), false);
    // check helper
    t.checkExpect(this.cell1.isLinkedHelp(this.cell2), false);
    t.checkExpect(this.cell2.isLinkedHelp(this.cell1), false);

  }

  void testSetEdge(Tester t) {
    this.initConds();

    // check that cell refers to its original state (own copy)
    t.checkExpect(this.cell1, this.cell1Copy);

    // update edges
    this.cell1.setEdge(this.edge1, 1);
    this.cell2.setEdge(this.edge1, 3);

    // check if they are connected
    t.checkExpect(this.cell1.isLinked(this.cell2), true);

    this.initConds();
    // update edges
    this.cell1.setEdge(this.edge1, 0);
    this.cell2.setEdge(this.edge1, 2);

    // check if they are connected
    t.checkExpect(this.cell1.isLinked(this.cell2), true);

    // check exceptions
    t.checkException(new RuntimeException("Direction must be between 0 and 3 inclusive!"),
        this.cell1, "setEdge", this.edge1, -1);
    t.checkException(new RuntimeException("Direction must be between 0 and 3 inclusive!"),
        this.cell1, "setEdge", this.edge1, 4);
  }

  void testSetLink(Tester t) {
    this.initConds();

    // make sure maze refers to its initial state (own copy)
    t.checkExpect(this.maze1, this.maze1Copy);

    // make sure this cell does not yet refer to second cell
    t.checkExpect(this.cell1.isLinked(this.cell2), false);

    // use setConnection
    this.maze1.setLink(this.cell1, this.cell2, 0, 5.0);

    // make sure this now refers to second cell
    t.checkExpect(this.cell1.isLinked(this.cell2), true);

    this.initConds();

    // make sure this cell does not yet refer to second cell
    t.checkExpect(this.cell1.isLinked(this.cell2), false);

    // use setConnection
    this.maze1.setLink(this.cell1, this.cell2, 1, 5.0);

    // make sure this now connects to second cell
    t.checkExpect(this.cell1.isLinked(this.cell2), true);

    this.initConds();

    // testing with other kinds of cells
    Cell cellWithRandomEdges = new Cell(this.edge1, new Edge(7.0),
        new Edge(this.cell1Copy, this.cellH2), new Edge(this.cellH3, this.cellH4, 70.0));

    // test with observatory method before setting the link
    t.checkExpect(cellWithRandomEdges.isLinked(cellWithRandomEdges), false);
    t.checkExpect(cellWithRandomEdges.isLinked(this.cell1), false);
    t.checkExpect(cell1.isLinked(cellWithRandomEdges), false);

    // method that changes the fields
    this.maze1.setLink(cellWithRandomEdges, this.cell1, 0, 0);

    // test with observatory method after setting the link
    t.checkExpect(cellWithRandomEdges.isLinked(this.cell1), true);
    t.checkExpect(this.cell1.isLinked(cellWithRandomEdges), true);

    // test self-linking
    this.maze1.setLink(cellWithRandomEdges, cellWithRandomEdges, 1, 7.0);
    t.checkExpect(cellWithRandomEdges.isLinked(cellWithRandomEdges), true);
  }

  void testLinkGrid(Tester t) {
    this.initConds();

    // initialize the cell grid
    ArrayList<ArrayList<Cell>> gridCell = new ArrayList<>();
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());

    Cell cell00 = new Cell();
    Cell cell01 = new Cell();
    Cell cell02 = new Cell();

    Cell cell10 = new Cell();
    Cell cell11 = new Cell();
    Cell cell12 = new Cell();

    Cell cell20 = new Cell();
    Cell cell21 = new Cell();
    Cell cell22 = new Cell();

    gridCell.get(0).add(cell00);
    gridCell.get(0).add(cell01);
    gridCell.get(0).add(cell02);

    gridCell.get(1).add(cell10);
    gridCell.get(1).add(cell11);
    gridCell.get(1).add(cell12);

    gridCell.get(2).add(cell20);
    gridCell.get(2).add(cell21);
    gridCell.get(2).add(cell22);

    new Maze(3, 3).linkGrid(gridCell, new Random(420), 0);

    // check connections for 00
    t.checkExpect(cell00.isLinked(cell01), true);
    t.checkExpect(cell00.isLinked(cell10), true);
    t.checkExpect(cell00.isLinked(cell11), false);

    t.checkExpect(cell00.isLinked(cell00), false);

    // check connections for 02
    t.checkExpect(cell02.isLinked(cell01), true);
    t.checkExpect(cell02.isLinked(cell12), true);
    t.checkExpect(cell02.isLinked(cell11), false);

    t.checkExpect(cell02.isLinked(cell02), false);

    // check connections for 20
    t.checkExpect(cell20.isLinked(cell10), true);
    t.checkExpect(cell20.isLinked(cell21), true);
    t.checkExpect(cell20.isLinked(cell11), false);

    t.checkExpect(cell20.isLinked(cell20), false);

    // check connections for 22
    t.checkExpect(cell22.isLinked(cell21), true);
    t.checkExpect(cell22.isLinked(cell12), true);
    t.checkExpect(cell22.isLinked(cell11), false);

    t.checkExpect(cell22.isLinked(cell22), false);

    // check connections for 01
    t.checkExpect(cell01.isLinked(cell00), true);
    t.checkExpect(cell01.isLinked(cell02), true);
    t.checkExpect(cell01.isLinked(cell11), true);
    t.checkExpect(cell01.isLinked(cell10), false);
    t.checkExpect(cell01.isLinked(cell12), false);

    t.checkExpect(cell01.isLinked(cell01), false);

    // check connections for 21
    t.checkExpect(cell21.isLinked(cell22), true);
    t.checkExpect(cell21.isLinked(cell20), true);
    t.checkExpect(cell21.isLinked(cell11), true);
    t.checkExpect(cell21.isLinked(cell10), false);
    t.checkExpect(cell21.isLinked(cell12), false);

    t.checkExpect(cell21.isLinked(cell21), false);

    // check connections for 10
    t.checkExpect(cell10.isLinked(cell00), true);
    t.checkExpect(cell10.isLinked(cell20), true);
    t.checkExpect(cell10.isLinked(cell11), true);
    t.checkExpect(cell10.isLinked(cell01), false);
    t.checkExpect(cell10.isLinked(cell21), false);

    t.checkExpect(cell10.isLinked(cell10), false);

    // check connections for 12
    t.checkExpect(cell12.isLinked(cell02), true);
    t.checkExpect(cell12.isLinked(cell22), true);
    t.checkExpect(cell12.isLinked(cell11), true);
    t.checkExpect(cell12.isLinked(cell01), false);
    t.checkExpect(cell12.isLinked(cell21), false);

    t.checkExpect(cell12.isLinked(cell12), false);

    // check connections for 11
    t.checkExpect(cell11.isLinked(cell01), true);
    t.checkExpect(cell11.isLinked(cell10), true);
    t.checkExpect(cell11.isLinked(cell21), true);
    t.checkExpect(cell11.isLinked(cell12), true);

    t.checkExpect(cell11.isLinked(cell00), false);
    t.checkExpect(cell11.isLinked(cell02), false);
    t.checkExpect(cell11.isLinked(cell20), false);
    t.checkExpect(cell11.isLinked(cell22), false);

    t.checkExpect(cell11.isLinked(cell11), false);
  }

  void testRenderEdge(Tester t) {
    this.initConds();
    Edge e = new Edge();

    // test edge is connected
    t.checkExpect(e.render(false), new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
    t.checkExpect(e.render(true), new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));

    // connected edge
    e.setTrue();

    // test edge is connected
    t.checkExpect(e.render(false), new EmptyImage());
    t.checkExpect(e.render(true), new EmptyImage());

  }

  void testRenderCell(Tester t) {
    this.initConds();

    // border cells render an empty image regardless of arguments
    t.checkExpect(this.borderCell1.render(false, 0), new EmptyImage());
    t.checkExpect(this.borderCell1.render(true, 999), new EmptyImage());

    // initialize 2x2 grid of cells
    Cell topLeft = new Cell();
    Cell topRight = new Cell();
    Cell bottomLeft = new Cell();
    Cell bottomRight = new Cell();

    Edge edgeTop = new Edge(topLeft, topRight);
    Edge edgeRight = new Edge(topRight, bottomRight);
    Edge edgeLeft = new Edge(bottomLeft, topLeft);
    Edge edgeBottom = new Edge(bottomLeft, bottomRight);

    topLeft.setEdge(edgeTop, 1);
    topLeft.setEdge(edgeLeft, 2);

    topRight.setEdge(edgeTop, 3);
    topRight.setEdge(edgeRight, 2);

    bottomLeft.setEdge(edgeBottom, 1);
    bottomLeft.setEdge(edgeLeft, 0);

    bottomRight.setEdge(edgeBottom, 3);
    bottomRight.setEdge(edgeRight, 0);

    // check render when false
    t.checkExpect(topLeft.render(false, -1),
        new AboveImage(topLeft.renderRow(false, -1), bottomLeft.render(false, -1)));

    // check render when true
    t.checkExpect(topLeft.render(true, -1),
        new AboveImage(topLeft.renderRow(true, -1), bottomLeft.render(true, -1)));

    // check different values of step gradient and boolean combos
    t.checkExpect(topLeft.render(true, 5),
        new AboveImage(topLeft.renderRow(true, 5), bottomLeft.render(true, 5)));
    t.checkExpect(topLeft.render(false, 5),
        new AboveImage(topLeft.renderRow(false, 5), bottomLeft.render(false, 5)));
    t.checkExpect(topLeft.render(true, 0),
        new AboveImage(topLeft.renderRow(true, 0), bottomLeft.render(true, 0)));
  }

  void testRenderRowCell(Tester t) {
    this.initConds();

    // border cells always render empty images, regardless of arguments
    t.checkExpect(this.borderCell1.renderRow(false, 0), new EmptyImage());
    t.checkExpect(this.borderCell1.renderRow(true, 222), new EmptyImage());
    t.checkExpect(new BorderCell().renderRow(true, 0), new EmptyImage());
    t.checkExpect(this.borderCell1.renderRow(false, 888), new EmptyImage());

    // initialize 2x2 grid of cells
    Cell topLeft = new Cell();
    Cell topRight = new Cell();
    Cell bottomLeft = new Cell();
    Cell bottomRight = new Cell();

    Edge edgeTop = new Edge(topLeft, topRight);
    Edge edgeRight = new Edge(topRight, bottomRight);
    Edge edgeLeft = new Edge(bottomLeft, topLeft);
    Edge edgeBottom = new Edge(bottomLeft, bottomRight);

    // top or right, node 2

    topLeft.setEdge(edgeTop, 1);
    topLeft.setEdge(edgeLeft, 2);

    topRight.setEdge(edgeTop, 3);
    topRight.setEdge(edgeRight, 2);

    bottomLeft.setEdge(edgeBottom, 1);
    bottomLeft.setEdge(edgeLeft, 0);

    bottomRight.setEdge(edgeBottom, 3);
    bottomRight.setEdge(edgeRight, 0);

    // test displayGradient
    // set display gradient to true with step counts from 0-2
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(1);
    bottomRight.setGradient(2);

    double scaledTo1 = (double) 0 / 2;
    int red = (int) (255 * (1 - scaledTo1));
    int blue = (int) (255 * scaledTo1);

    // check true case
    t.checkExpect(topLeft.renderRow(false, 2),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(red, 0, blue))),
            topRight.renderRow(false, 2)));

    // check false case
    t.checkExpect(topLeft.renderRow(false, 2),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(red, 0, blue))),
            topRight.renderRow(false, 2)));

    // reset displayGradient
    topLeft.resetGradient();
    topRight.resetGradient();
    bottomLeft.resetGradient();
    bottomRight.resetGradient();

    // set solution
    topLeft.setSolution(true);
    topRight.setSolution(true);
    bottomLeft.setSolution(true);
    bottomRight.setSolution(true);

    // test solution
    t.checkExpect(topLeft.renderRow(false, -1),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            topRight.renderRow(false, -1)));

    t.checkExpect(topLeft.renderRow(true, -1),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            topRight.renderRow(true, -1)));

    t.checkExpect(topLeft.renderRow(false, 3),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            topRight.renderRow(false, 3)));

    // clear solution
    topLeft.setSolution(false);
    topRight.setSolution(false);
    bottomLeft.setSolution(false);
    bottomRight.setSolution(false);

    // indicate player
    topLeft.setPlayerIndicator(true);

    // test player tile
    t.checkExpect(topLeft.renderRow(false, -1),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176))),
            topRight.renderRow(false, -1)));
    t.checkExpect(topLeft.renderRow(true, -1),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176))),
            topRight.renderRow(true, -1)));
    t.checkExpect(topLeft.renderRow(false, 6),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176))),
            topRight.renderRow(true, 6)));

    // reset player indicator
    topLeft.setPlayerIndicator(false);

    // this.visited is true, but show visited is false
    topLeft.setVisited(true);
    topRight.setVisited(true);
    bottomLeft.setVisited(true);
    bottomRight.setVisited(true);

    t.checkExpect(topLeft.renderRow(false, 6),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(false, 6)));
    t.checkExpect(topLeft.renderRow(false, 0),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(false, 0)));
    t.checkExpect(topLeft.renderRow(false, -1),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(false, -1)));

    // this.visited is true, and show visited is true
    t.checkExpect(topLeft.renderRow(true, 6),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233))),
            topRight.renderRow(true, 6)));
    t.checkExpect(topLeft.renderRow(true, 0),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233))),
            topRight.renderRow(true, 0)));
    t.checkExpect(topLeft.renderRow(true, -1),
        new BesideImage(
            topLeft
                .drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233))),
            topRight.renderRow(true, -1)));

    // base case
    topLeft.setVisited(false);
    topRight.setVisited(false);
    bottomLeft.setVisited(false);
    bottomRight.setVisited(false);

    t.checkExpect(topLeft.renderRow(false, 6),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(false, 6)));
    t.checkExpect(topLeft.renderRow(true, 0),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(true, 0)));
    t.checkExpect(topLeft.renderRow(true, -1),
        new BesideImage(
            topLeft.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            topRight.renderRow(true, -1)));
  }

  void testAsCell(Tester t) {
    this.initConds();

    // border cells will always throw exceptions since border cells are not regular
    // cells
    t.checkException(new RuntimeException("Cannot call asCell on border cell!"), this.borderCell1,
        "asCell");

    // test asCell on Cell class
    Cell cell = new Cell();
    t.checkExpect(cell.asCell(), cell);

  }

  void testFindMaxSteps(Tester t) {
    this.initConds();

    // border cells always return the argument value for max steps, face value
    t.checkExpect(this.borderCell1.findMaxSteps(0), 0);
    t.checkExpect(this.borderCell1.findMaxSteps(999), 999);
    t.checkExpect(this.borderCell1.findMaxSteps(-1), -1);

    // initialize 2x2 grid of cells
    Cell topLeft = new Cell();
    Cell topRight = new Cell();
    Cell bottomLeft = new Cell();
    Cell bottomRight = new Cell();

    Edge edgeTop = new Edge(topLeft, topRight);
    Edge edgeRight = new Edge(topRight, bottomRight);
    Edge edgeLeft = new Edge(bottomLeft, topLeft);
    Edge edgeBottom = new Edge(bottomLeft, bottomRight);

    // top or right, node 2

    topLeft.setEdge(edgeTop, 1);
    topLeft.setEdge(edgeLeft, 2);

    topRight.setEdge(edgeTop, 3);
    topRight.setEdge(edgeRight, 2);

    bottomLeft.setEdge(edgeBottom, 1);
    bottomLeft.setEdge(edgeLeft, 0);

    bottomRight.setEdge(edgeBottom, 3);
    bottomRight.setEdge(edgeRight, 0);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(1);
    bottomRight.setGradient(2);

    t.checkExpect(topLeft.findMaxSteps(0), 2);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(1);
    bottomRight.setGradient(2);

    t.checkExpect(topLeft.findMaxSteps(0), 2);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(0);
    bottomRight.setGradient(2);

    t.checkExpect(topLeft.findMaxSteps(0), 2);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(0);
    bottomLeft.setGradient(0);
    bottomRight.setGradient(0);

    t.checkExpect(topLeft.findMaxSteps(0), 0);

  }

  void testFindMaxStepsInRow(Tester t) {
    this.initConds();

    // border cells always return the argument value for max steps in row, face
    // value
    t.checkExpect(this.borderCell1.findMaxStepsInRow(0), 0);
    t.checkExpect(this.borderCell1.findMaxStepsInRow(-1), -1);
    t.checkExpect(this.borderCell1.findMaxStepsInRow(999), 999);

    // initialize 2x2 grid of cells
    Cell topLeft = new Cell();
    Cell topRight = new Cell();
    Cell bottomLeft = new Cell();
    Cell bottomRight = new Cell();

    Edge edgeTop = new Edge(topLeft, topRight);
    Edge edgeRight = new Edge(topRight, bottomRight);
    Edge edgeLeft = new Edge(bottomLeft, topLeft);
    Edge edgeBottom = new Edge(bottomLeft, bottomRight);

    // top or right, node 2

    topLeft.setEdge(edgeTop, 1);
    topLeft.setEdge(edgeLeft, 2);

    topRight.setEdge(edgeTop, 3);
    topRight.setEdge(edgeRight, 2);

    bottomLeft.setEdge(edgeBottom, 1);
    bottomLeft.setEdge(edgeLeft, 0);

    bottomRight.setEdge(edgeBottom, 3);
    bottomRight.setEdge(edgeRight, 0);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(1);
    bottomRight.setGradient(2);

    t.checkExpect(topLeft.findMaxStepsInRow(0), 1);

    // set steps
    topLeft.setGradient(1);
    topRight.setGradient(0);
    bottomLeft.setGradient(1);
    bottomRight.setGradient(2);

    t.checkExpect(topLeft.findMaxSteps(0), 2);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(1);
    bottomLeft.setGradient(0);
    bottomRight.setGradient(2);

    t.checkExpect(bottomLeft.findMaxSteps(0), 2);

    // set steps
    topLeft.setGradient(0);
    topRight.setGradient(0);
    bottomLeft.setGradient(0);
    bottomRight.setGradient(0);

    t.checkExpect(bottomLeft.findMaxSteps(0), 0);
  }

  void testGetWeight(Tester t) {
    this.initConds();

    t.checkExpect(new Edge().getWeight(), -1.0);
    Edge edge = new Edge(1);
    t.checkExpect(edge.getWeight(), 1.0);
    t.checkExpect(this.edge1.getWeight(), -1.0);
    t.checkExpect(new Edge(this.cellH1, this.cellH2, 20.0).getWeight(), 20.0);

    Edge edgeWithDirs1 = new Edge(this.cellH2, this.cellH4);
    Edge edgeWithDirs2 = new Edge(this.cellH2, this.cellH4, 88.0);
    t.checkExpect(edgeWithDirs1.getWeight(), -1.0);
    t.checkExpect(edgeWithDirs2.getWeight(), 88.0);
  }

  void testCompareEdges(Tester t) {
    this.initConds();

    CompareEdges compareEdges = new CompareEdges();
    t.checkExpect(compareEdges.compare(new Edge(1), new Edge(1)), 0);
    t.checkExpect(compareEdges.compare(new Edge(0), new Edge(1)), -1);
    t.checkExpect(compareEdges.compare(new Edge(1), new Edge(0)), 1);
  }

  void testFind(Tester t) {
    this.initConds();

    // check for recursive calls
    t.checkExpect(new Maze(6, 9).find(this.rep, this.cellH1), this.cellH1);
    t.checkExpect(new Maze(6, 9).find(this.rep, this.cellH2), this.cellH1);
    t.checkExpect(new Maze(6, 9).find(this.rep, this.cellH3), this.cellH1);
    t.checkExpect(new Maze(6, 9).find(this.rep, this.cellH4), this.cellH4);

    // check for exception
    t.checkException(new RuntimeException("Node is not here"), new Maze(6, 9), "find", this.rep,
        new Cell());

    // check other kinds of mazes
    t.checkExpect(this.maze1.find(this.rep, this.cellH1), this.cellH1);
    t.checkException(new RuntimeException("Node is not here"), this.maze1, "find", this.rep,
        this.borderCell1);
    Cell randomCell = new Cell(new Edge(9.9), new Edge(99.9), new Edge(999.9), new Edge(-1.0));
    HashMap<ICell, ICell> randomReps = new HashMap<>();
    randomReps.put(randomCell, randomCell);
    randomReps.put(this.cell1, this.cellH1);
    randomReps.put(this.cellH2, this.cellH2);
    randomReps.put(new Cell(), this.cell1Copy);

    // more cases
    t.checkExpect(new Maze(100, 60).find(randomReps, randomCell), randomCell);
    t.checkExpect(new Maze(42, 2).find(randomReps, this.cellH2), this.cellH2);

    // remove something from our random reps
    randomReps.remove(new Cell());
    t.checkException(new RuntimeException("Node is not here"), this.maze1, "find", randomReps,
        new Cell());

    // re-add and test the rest
    randomReps.put(this.cell1Copy, this.cell1Copy);
    t.checkExpect(this.maze1.find(randomReps, this.cell1Copy), this.cell1Copy);
    t.checkException(new RuntimeException("Node is not here"), new Maze(99, 99), "find", randomReps,
        this.cell1);
    t.checkException(new RuntimeException("Node is not here"), new Maze(3, 5), "find", randomReps,
        this.cellH1);
    t.checkException(new RuntimeException("Node is not here"), new Maze(9, 6), "find", randomReps,
        new BorderCell());
  }

  void testUnion(Tester t) {
    this.initConds();

    // union
    new Maze(6, 9).union(this.rep, this.cellH1, this.cellH2);
    // check hashmap for union operation
    t.checkExpect(this.rep.get(this.cellH1), this.cellH1);

    // reset
    this.initConds();
    // union
    new Maze(6, 9).union(this.rep, this.cellH4, this.cellH1);
    // check hashmap for union operation
    t.checkExpect(this.rep.get(this.cellH4), this.cellH1);

    // reset
    this.initConds();
    // union
    new Maze(6, 9).union(this.rep, this.cellH3, this.cellH2);
    // check hashmap for union operation
    t.checkExpect(this.rep.get(this.rep.get(this.cellH3)), this.cellH1);

    // reset
    this.initConds();
    // union
    new Maze(6, 9).union(this.rep, this.cellH2, this.cellH4);
    // check hashmap for union operation
    t.checkExpect(this.rep.get(this.rep.get(this.cellH2)), this.cellH4);

    // reset
    this.initConds();
    // union
    new Maze(6, 9).union(this.rep, this.cellH3, this.cellH4);
    // check hashmap for union operation
    t.checkExpect(this.rep.get(this.rep.get(this.rep.get(this.cellH3))), this.cellH4);

    // reset
    this.initConds();
    // other cases
    HashMap<ICell, ICell> otherReps = new HashMap<>();
    otherReps.put(this.cellH1, this.cellH1);
    otherReps.put(this.cellH2, this.cellH1);
    otherReps.put(this.cellH4, this.cellH2);

    // check our reps
    t.checkExpect(otherReps.get(this.cellH4), this.cellH2);
    t.checkExpect(otherReps.get(this.cellH2), this.cellH1);
    t.checkExpect(otherReps.get(this.cellH1), this.cellH1);

    // union
    this.maze1.union(otherReps, this.cellH4, this.cellH2);

    // check hashmap to verify union
    // nothing should change since find would return the same thing based on these
    // premises
    t.checkExpect(otherReps.get(otherReps.get(otherReps.get(otherReps.get(this.cellH2)))),
        this.cellH1);
    t.checkExpect(otherReps.get(this.cellH1), this.cellH1);
  }

  void testWholeWorld(Tester t) {
    this.initConds();

    // no bias game: 100 x 60
    this.game.bigBang(1000, 1000, 0.01);
    // horizontal game: 40 x 25
    this.gameHorizontal.bigBang(1000, 1000, 0.01);
    // vertical game: 40 x 25
    this.gameVertical.bigBang(1000, 1000, 0.01);
    // mini game! no bias
    this.gameTiny.bigBang(1000, 1000, 0.01);
  }

  void testAddEdges(Tester t) {
    this.initConds();

    // make list of edges
    ArrayList<Edge> edges = new ArrayList<>();
    Edge edgeRight = new Edge();
    Edge edgeBottom = new Edge();

    Cell cell1 = new Cell(new Edge(), edgeRight, edgeBottom, new Edge());

    // add right edge
    cell1.addEdges(edges, 1);

    // check that right edge added
    t.checkExpect(edges.get(0), edgeRight);

    // add right edge
    cell1.addEdges(edges, 2);

    // check that bottom edge added
    t.checkExpect(edges.get(1), edgeBottom);

  }

  void testGetNodes(Tester t) {
    this.initConds();

    // test cases with default cells
    Cell cell1 = new Cell();
    Cell cell2 = new Cell();

    Edge basicEdge = new Edge(cell1, cell2);
    t.checkExpect(basicEdge.getNode1(), cell1);
    t.checkExpect(basicEdge.getNode2(), cell2);
    t.checkExpect(this.edge1.getNode1(), new Cell());
    t.checkExpect(this.edge1.getNode2(), new Cell());

    // test cases with edges that have no arguments passed in the constructor
    Edge emptyEdge = new Edge();
    t.checkExpect(emptyEdge.getNode1(), new BorderCell());
    t.checkExpect(emptyEdge.getNode2(), new BorderCell());

    // test cases with cells with all directions
    Edge edgeDir1 = new Edge(this.cellH1, this.cellH2);
    Edge edgeDir2 = new Edge(this.cellH3, this.cellH4);

    t.checkExpect(edgeDir1.getNode1(), this.cellH1);
    t.checkExpect(edgeDir1.getNode2(), this.cellH2);
    t.checkExpect(edgeDir2.getNode1(), this.cellH3);
    t.checkExpect(edgeDir2.getNode2(), this.cellH4);

    // test edge weight
    Edge edgeWeight1 = new Edge(-1.0);
    Edge edgeWeight2 = new Edge(1.0);
    t.checkExpect(edgeWeight1.getNode1(), null);
    t.checkExpect(edgeWeight1.getNode2(), null);
    t.checkExpect(edgeWeight2.getNode1(), null);
    t.checkExpect(edgeWeight2.getNode2(), null);
  }

  void testSetTrue(Tester t) {
    this.initConds();

    // make sure edge refers to false as in, render which observes the connected
    // field
    // indicates that the edge refers to false if a border is generated
    // otherwise an empty image is generated upon using setTrue, regardless if
    // vertical
    // cases for true or false
    t.checkExpect(this.edge1.render(false),
        new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
    t.checkExpect(this.edge1.render(true),
        new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));

    // set field to true
    this.edge1.setTrue();

    // check change, should produce an empty image regardless
    t.checkExpect(this.edge1.render(false), new EmptyImage());
    t.checkExpect(this.edge1.render(true), new EmptyImage());

    // test edge with complete fields, should be the same regardless
    Edge edge2 = new Edge(this.cellH1, this.cellH2, 42.0);
    t.checkExpect(edge2.render(false), new RectangleImage(10, 1, OutlineMode.SOLID, Color.black));
    t.checkExpect(edge2.render(true), new RectangleImage(1, 10, OutlineMode.SOLID, Color.black));

    // set field to true
    edge2.setTrue();

    // check change, should produce an empty image regardless
    t.checkExpect(edge2.render(false), new EmptyImage());
    t.checkExpect(edge2.render(true), new EmptyImage());
  }

  void testAddUniqueEdges(Tester t) {
    this.initConds();

    // initialize nodeGrid
    ArrayList<ArrayList<Cell>> nodeGrid = new ArrayList<>();
    // adds 3 rows of cell lists
    for (int rowIndex = 0; rowIndex < 3; rowIndex += 1) {
      nodeGrid.add(new ArrayList<>());
    }

    // adds 3 columns to each row 3 times
    for (int rowIndex = 0; rowIndex < 3; rowIndex += 1) {
      // adds 3 columns to this row
      for (int colIndex = 0; colIndex < 3; colIndex += 1) {
        nodeGrid.get(rowIndex).add(new Cell());
      }
    }

    // 3x3 has 12 unique edges
    t.checkExpect(new Maze(3, 3).addUniqueEdges(nodeGrid, 3, 3).size(), 12);

    // initialize nodeGrid2
    ArrayList<ArrayList<Cell>> nodeGrid2 = new ArrayList<>();
    // adds 4 rows of cell lists
    for (int rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      nodeGrid2.add(new ArrayList<>());
    }

    // adds 13 columns to each row 4 times
    for (int rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      // adds 13 columns to this row
      for (int colIndex = 0; colIndex < 13; colIndex += 1) {
        nodeGrid2.get(rowIndex).add(new Cell());
      }
    }

    // 4x13 has 87 unique edges
    t.checkExpect(new Maze(4, 13).addUniqueEdges(nodeGrid2, 4, 13).size(), 87);
    // number of rows passed must be the same as the number of rows in the grid
    t.checkException(new IndexOutOfBoundsException("Index 4 out of bounds for length 4"),
        new Maze(5, 5), "addUniqueEdges", nodeGrid2, 5, 13);
  }

  void testPair(Tester t) {
    this.initConds();

    // testing simpler data like Integer
    Pair<Double, Double> pairOfDoubles = new Pair<>(7.0, -1.0);
    t.checkExpect(pairOfDoubles.getObj1(), 7.0);
    t.checkExpect(pairOfDoubles.getObj2(), -1.0);

    // testing a pair of more complex data like cells
    Pair<Cell, Cell> pairOfCells = new Pair<>(this.cellH1, this.cellH2);
    t.checkExpect(pairOfCells.getObj1(), this.cellH1);
    t.checkExpect(pairOfCells.getObj2(), this.cellH2);

    // test two types
    Pair<String, Edge> strAndEdge = new Pair<>("edge", this.edge1);
    t.checkExpect(strAndEdge.getObj1(), "edge");
    t.checkExpect(strAndEdge.getObj2(), this.edge1);
  }

  void testChangeIfCan(Tester t) {
    this.initConds();

    // base only matters if the direction is unknown or the input is not read by
    // the if statement logic

    // test default edge
    // should return a default cell
    t.checkExpect(this.edge1.changeIfCan("up", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("right", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("down", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("left", this.cell1), new Cell());
    // for unknown direction
    t.checkExpect(this.edge1.changeIfCan("55555555555", this.cell1), new Cell());

    // change connection state via setTrue
    this.edge1.setTrue();

    t.checkExpect(this.edge1.changeIfCan("up", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("right", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("down", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("left", this.cell1), new Cell());
    t.checkExpect(this.edge1.changeIfCan("55555555555", this.cell1), new Cell());

    // test edges with cells that also have a top, right, bottom, left
    // since connection state is false, they return the base
    Edge edgeDir = new Edge(cellH1, cellH4);
    t.checkExpect(edgeDir.changeIfCan("up", this.cellH1), this.cellH1);
    t.checkExpect(edgeDir.changeIfCan("right", this.cellH2), this.cellH2);
    t.checkExpect(edgeDir.changeIfCan("down", this.cellH3), this.cellH3);
    t.checkExpect(edgeDir.changeIfCan("left", this.cellH4), this.cellH4);
    // test a non-existent direction
    t.checkExpect(edgeDir.changeIfCan("nothing", this.cell1), this.cell1);

    // change connection state via setTrue
    edgeDir.setTrue();

    t.checkExpect(edgeDir.changeIfCan("up", this.cellH1), this.cellH4);
    t.checkExpect(edgeDir.changeIfCan("right", this.cellH2), this.cellH4);
    t.checkExpect(edgeDir.changeIfCan("down", this.cellH3), this.cellH1);
    t.checkExpect(edgeDir.changeIfCan("left", this.cellH4), this.cellH1);
    t.checkExpect(edgeDir.changeIfCan("directionless", new Cell()), new Cell());

    // test edge weight
    Edge edgeWeight = new Edge(1.0);

    t.checkExpect(edgeWeight.changeIfCan("up", this.cell1), this.cell1);
    t.checkExpect(edgeWeight.changeIfCan("right", this.cell1), this.cell1);
    t.checkExpect(edgeWeight.changeIfCan("down", this.cell1), this.cell1);
    t.checkExpect(edgeWeight.changeIfCan("left", this.cell1), this.cell1);
    // test a non-existent direction
    t.checkExpect(edgeWeight.changeIfCan("nothing", this.cell1), this.cell1);
  }

  void testAddIfPossible(Tester t) {
    this.initConds();

    // regardless of any type of edge, if the direction is
    // less than 0 or greater than 3, an error is thrown
    t.checkException(new RuntimeException("Direction must be between 0 and 3 inclusive"),
        this.edge1, "addIfPossible", -1);
    t.checkException(new RuntimeException("Direction must be between 0 and 3 inclusive"),
        this.edge1, "addIfPossible", 4);

    // test default edge for add if possible
    List<Cell> expectedDefault = new ArrayList<>();
    expectedDefault.add(new Cell());

    // since the edge is not connected yet, it's going to be an empty list
    t.checkExpect(this.edge1.addIfPossible(0), new ArrayList<>());
    t.checkExpect(this.edge1.addIfPossible(1), new ArrayList<>());
    t.checkExpect(this.edge1.addIfPossible(2), new ArrayList<>());
    t.checkExpect(this.edge1.addIfPossible(3), new ArrayList<>());

    // set connection to true via setTrue
    this.edge1.setTrue();

    t.checkExpect(this.edge1.addIfPossible(0), expectedDefault);
    t.checkExpect(this.edge1.addIfPossible(1), expectedDefault);
    t.checkExpect(this.edge1.addIfPossible(2), expectedDefault);
    t.checkExpect(this.edge1.addIfPossible(3), expectedDefault);

    // test edge full of cells
    Edge fullEdge = new Edge(this.cellH1, this.cellH2);
    // by now we know that any supplied direction will still result in an
    // empty list since the connection hasn't been set to true so the
    // function ignores the direction, testing just in case
    t.checkExpect(fullEdge.addIfPossible(0), new ArrayList<>());
    t.checkExpect(fullEdge.addIfPossible(1), new ArrayList<>());
    t.checkExpect(fullEdge.addIfPossible(2), new ArrayList<>());
    t.checkExpect(fullEdge.addIfPossible(3), new ArrayList<>());

    // set connection to true via setTrue
    fullEdge.setTrue();

    // pass expected nodes into list
    List<Cell> expectNode1 = new ArrayList<>();
    List<Cell> expectedNode2 = new ArrayList<>();
    expectNode1.add(this.cellH1);
    expectedNode2.add(this.cellH2);
    t.checkExpect(fullEdge.addIfPossible(0), expectedNode2);
    t.checkExpect(fullEdge.addIfPossible(1), expectedNode2);
    t.checkExpect(fullEdge.addIfPossible(2), expectNode1);
    t.checkExpect(fullEdge.addIfPossible(3), expectNode1);
  }

  void testDrawCell(Tester t) {
    this.initConds();

    // initialize cell
    Edge top = new Edge();
    Edge right = new Edge();
    Edge bottom = new Edge();
    Edge left = new Edge();

    top.setTrue();
    left.setTrue();

    Cell cell = new Cell(top, right, bottom, left);

    // set start node
    cell.setStart();

    WorldImage returnImage = new OverlayImage(
        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)), new EmptyImage());

    // test if start node
    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(left.render(true), 4.5, 0,
            new OverlayOffsetImage(bottom.render(false), 0, -4.5,
                new OverlayOffsetImage(top.render(false), 0, 4.5,
                    new OverlayOffsetImage(right.render(true), -4.5, 0, returnImage)))));

    // set end node
    cell = new Cell(top, right, bottom, left);

    cell.setEnd();

    returnImage = new OverlayImage(
        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)), new EmptyImage());

    // test if end node
    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(left.render(true), 4.5, 0,
            new OverlayOffsetImage(bottom.render(false), 0, -4.5,
                new OverlayOffsetImage(top.render(false), 0, 4.5,
                    new OverlayOffsetImage(right.render(true), -4.5, 0, returnImage)))));

    // test if neither start or end node

    cell = new Cell(top, right, bottom, left);

    returnImage = new EmptyImage();

    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(left.render(true), 4.5, 0,
            new OverlayOffsetImage(bottom.render(false), 0, -4.5,
                new OverlayOffsetImage(top.render(false), 0, 4.5,
                    new OverlayOffsetImage(right.render(true), -4.5, 0, returnImage)))));

  }

  void testDrawPathBack(Tester t) {
    this.initConds();

    // intiialize hashmap and cells
    HashMap<Cell, Cell> references = new HashMap<>();

    Cell cell00 = new Cell();
    Cell cell01 = new Cell();
    Cell cell02 = new Cell();

    Cell cell10 = new Cell();
    Cell cell11 = new Cell();
    Cell cell12 = new Cell();

    Cell cell20 = new Cell();
    Cell cell21 = new Cell();
    Cell cell22 = new Cell();

    // create relevant mappings
    references.put(cell22, cell21);
    references.put(cell21, cell11);
    references.put(cell11, cell10);
    references.put(cell10, cell00);

    // create junk mappings
    references.put(cell20, cell21);
    references.put(cell12, cell11);
    references.put(cell02, cell12);
    references.put(cell01, cell00);

    // check that all the cells are not solution path
    t.checkExpect(cell22.render(false, -1),
        new AboveImage(new BesideImage(
            cell22.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell21.render(false, -1),
        new AboveImage(new BesideImage(
            cell21.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell11.render(false, -1),
        new AboveImage(new BesideImage(
            cell11.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell10.render(false, -1),
        new AboveImage(new BesideImage(
            cell10.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell00.render(false, -1),
        new AboveImage(new BesideImage(
            cell00.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));

    t.checkExpect(cell20.render(false, -1),
        new AboveImage(new BesideImage(
            cell20.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell12.render(false, -1),
        new AboveImage(new BesideImage(
            cell12.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell02.render(false, -1),
        new AboveImage(new BesideImage(
            cell02.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell01.render(false, -1),
        new AboveImage(new BesideImage(
            cell01.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));

    // draw path back
    cell22.drawPathBack(references);

    // check if solution path changed and nothing else
    t.checkExpect(cell20.render(false, -1),
        new AboveImage(new BesideImage(
            cell20.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell12.render(false, -1),
        new AboveImage(new BesideImage(
            cell12.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell02.render(false, -1),
        new AboveImage(new BesideImage(
            cell02.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell01.render(false, -1),
        new AboveImage(new BesideImage(
            cell01.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()), new EmptyImage()));

    t.checkExpect(cell22.render(false, -1),
        new AboveImage(new BesideImage(
            cell22.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell21.render(false, -1),
        new AboveImage(new BesideImage(
            cell21.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell11.render(false, -1),
        new AboveImage(new BesideImage(
            cell11.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell10.render(false, -1),
        new AboveImage(new BesideImage(
            cell10.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()), new EmptyImage()));
    t.checkExpect(cell00.render(false, -1),
        new AboveImage(new BesideImage(
            cell00.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()), new EmptyImage()));

  }

  void testAddConnectedNeighbors(Tester t) {
    this.initConds();

    // test default cell
    List<Cell> emptyCellList = new ArrayList<>();
    List<Cell> expectedNeighborsDefault = new ArrayList<>();

    // add new default cell 4 times since it will be the same
    for (int i = 0; i < 4; i += 1) {
      expectedNeighborsDefault.add(new Cell());
    }
    // should be empty since the connection is false
    t.checkExpect(this.cell1.addConnectedNeighbors(), emptyCellList);
    t.checkExpect(this.cellH1.addConnectedNeighbors(), emptyCellList);

    // set up edges and set connected to true
    Edge edgeWithDefaultCells = new Edge(new Cell(), new Cell(), 50);
    edgeWithDefaultCells.setTrue();
    Cell plainCell = new Cell(edgeWithDefaultCells, edgeWithDefaultCells, edgeWithDefaultCells,
        edgeWithDefaultCells);

    t.checkExpect(plainCell.addConnectedNeighbors(), expectedNeighborsDefault);

    // set up edges and set connected to true via setTrue for edges before passing
    // then into a cell
    Edge complexE1 = new Edge(this.cell1, this.cellH1, 1);
    Edge complexE2 = new Edge(this.cellH2, this.cell1, 2);
    Edge complexE3 = new Edge(this.cell1, this.cellH3, 3);
    Edge complexE4 = new Edge(this.cellH4, this.cell1, 4);
    complexE1.setTrue();
    complexE2.setTrue();
    complexE3.setTrue();
    complexE4.setTrue();
    Cell cellOfFour = new Cell(complexE1, complexE2, complexE3, complexE4);

    List<Cell> expectedCo4Neighbors = new ArrayList<>();
    expectedCo4Neighbors.add(this.cellH1);
    expectedCo4Neighbors.add(this.cell1);
    expectedCo4Neighbors.add(this.cell1);
    expectedCo4Neighbors.add(this.cellH4);

    t.checkExpect(cellOfFour.addConnectedNeighbors(), expectedCo4Neighbors);
  }

  void testMoveIfCan(Tester t) {
    this.initConds();

    // initialize 2x2 grid of cells
    Cell topLeft = new Cell();
    Cell topRight = new Cell();
    Cell bottomLeft = new Cell();
    Cell bottomRight = new Cell();

    Edge edgeTop = new Edge(topLeft, topRight);
    Edge edgeRight = new Edge(topRight, bottomRight);
    Edge edgeLeft = new Edge(bottomLeft, topLeft);
    Edge edgeBottom = new Edge(bottomLeft, bottomRight);

    topLeft.setEdge(edgeTop, 1);
    topLeft.setEdge(edgeLeft, 2);

    topRight.setEdge(edgeTop, 3);
    topRight.setEdge(edgeRight, 2);

    bottomLeft.setEdge(edgeBottom, 1);
    bottomLeft.setEdge(edgeLeft, 0);

    bottomRight.setEdge(edgeBottom, 3);
    bottomRight.setEdge(edgeRight, 0);

    // make sure invalid move doesnt work
    t.checkExpect(topLeft.moveIfCan("up"), topLeft);
    t.checkExpect(topRight.moveIfCan("left"), topRight);
    t.checkExpect(bottomLeft.moveIfCan("right"), bottomLeft);
    t.checkExpect(bottomRight.moveIfCan("down"), bottomRight);

    // set edges to true
    edgeTop.setTrue();
    edgeRight.setTrue();
    edgeLeft.setTrue();
    edgeBottom.setTrue();

    // make sure can move if possible
    t.checkExpect(topRight.moveIfCan("down"), topRight);

    t.checkExpect(topLeft.moveIfCan("right"), topRight);
    t.checkExpect(bottomLeft.moveIfCan("up"), topLeft);
    t.checkExpect(bottomRight.moveIfCan("left"), bottomLeft);

    // check exception
    t.checkException(new RuntimeException("Invalid direction given to moveIfCan"), topLeft,
        "moveIfCan", "a");
  }

  void testLabelCorrectPath(Tester t) {
    this.initConds();

    // intiialize hashmap and cells
    HashMap<Cell, Cell> references = new HashMap<>();

    Cell cell00 = new Cell();
    Cell cell01 = new Cell();
    Cell cell02 = new Cell();

    Cell cell10 = new Cell();
    Cell cell11 = new Cell();
    Cell cell12 = new Cell();

    Cell cell20 = new Cell();
    Cell cell21 = new Cell();
    Cell cell22 = new Cell();

    // set visited to false to observe the correctPath field
    cell00.setVisited(true);
    cell01.setVisited(true);
    cell02.setVisited(true);

    cell10.setVisited(true);
    cell11.setVisited(true);
    cell12.setVisited(true);

    cell20.setVisited(true);
    cell21.setVisited(true);
    cell22.setVisited(true);

    // create relevant mappings
    references.put(cell22, cell21);
    references.put(cell21, cell11);
    references.put(cell11, cell10);
    references.put(cell10, cell00);

    // create junk mappings
    references.put(cell20, cell21);
    references.put(cell12, cell11);
    references.put(cell02, cell12);
    references.put(cell01, cell00);

    // check that all the cells are not correct path
    t.checkExpect(cell22.uniqueIncorrectMove(), false);
    t.checkExpect(cell21.uniqueIncorrectMove(), false);
    t.checkExpect(cell11.uniqueIncorrectMove(), false);
    t.checkExpect(cell10.uniqueIncorrectMove(), false);
    t.checkExpect(cell01.uniqueIncorrectMove(), false);

    t.checkExpect(cell01.uniqueIncorrectMove(), false);
    t.checkExpect(cell02.uniqueIncorrectMove(), false);
    t.checkExpect(cell12.uniqueIncorrectMove(), false);
    t.checkExpect(cell20.uniqueIncorrectMove(), false);

    // label correct cells
    cell22.labelCorrectPath(references);

    // check if correct path cells were changed and nothing else
    t.checkExpect(cell22.uniqueIncorrectMove(), false);
    t.checkExpect(cell21.uniqueIncorrectMove(), false);
    t.checkExpect(cell11.uniqueIncorrectMove(), false);
    t.checkExpect(cell10.uniqueIncorrectMove(), false);
    t.checkExpect(cell01.uniqueIncorrectMove(), false);

    // set visited to false to observe the correctPath field
    cell00.setVisited(false);
    cell01.setVisited(false);
    cell02.setVisited(false);

    cell10.setVisited(false);
    cell11.setVisited(false);
    cell12.setVisited(false);

    cell20.setVisited(false);
    cell21.setVisited(false);
    cell22.setVisited(false);

    t.checkExpect(cell01.uniqueIncorrectMove(), true);
    t.checkExpect(cell02.uniqueIncorrectMove(), true);
    t.checkExpect(cell12.uniqueIncorrectMove(), true);
    t.checkExpect(cell20.uniqueIncorrectMove(), true);
  }

  void testTraverse(Tester t) {
    this.initConds();

    // initialize the cell grid
    ArrayList<ArrayList<Cell>> gridCell = new ArrayList<>();
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());

    Cell cell00 = new Cell();
    Cell cell01 = new Cell();
    Cell cell02 = new Cell();

    Cell cell10 = new Cell();
    Cell cell11 = new Cell();
    Cell cell12 = new Cell();

    Cell cell20 = new Cell();
    Cell cell21 = new Cell();
    Cell cell22 = new Cell();

    gridCell.get(0).add(cell00);
    gridCell.get(0).add(cell01);
    gridCell.get(0).add(cell02);

    gridCell.get(1).add(cell10);
    gridCell.get(1).add(cell11);
    gridCell.get(1).add(cell12);

    gridCell.get(2).add(cell20);
    gridCell.get(2).add(cell21);
    gridCell.get(2).add(cell22);

    new Maze(3, 3).linkGrid(gridCell, new Random(420), 0);
    new Maze(3, 3).createMinimumSpanning(gridCell, false);

    // check lack of gradient before
    t.checkExpect(cell00.renderRow(false, 0),
        new BesideImage(cell00.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell01.renderRow(false, 0)));
    t.checkExpect(cell01.renderRow(false, 0),
        new BesideImage(cell01.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell02.renderRow(false, 0)));
    t.checkExpect(cell02.renderRow(false, 0),
        new BesideImage(cell02.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    t.checkExpect(cell10.renderRow(false, 0),
        new BesideImage(cell10.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell11.renderRow(false, 0)));
    t.checkExpect(cell11.renderRow(false, 0),
        new BesideImage(cell11.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell12.renderRow(false, 0)));
    t.checkExpect(cell12.renderRow(false, 0),
        new BesideImage(cell12.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    t.checkExpect(cell20.renderRow(false, 0),
        new BesideImage(cell20.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell21.renderRow(false, 0)));
    t.checkExpect(cell21.renderRow(false, 0),
        new BesideImage(cell21.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell22.renderRow(false, 0)));
    t.checkExpect(cell22.renderRow(false, 0),
        new BesideImage(cell22.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    // traverse
    cell00.traverse(0, new ArrayList<>(), false);

    // check for gradient after
    t.checkExpect(cell00.renderRow(false, 5),
        new BesideImage(
            cell00.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))),
            cell01.renderRow(false, 5)));
    t.checkExpect(cell01.renderRow(false, 5),
        new BesideImage(
            cell01.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(204, 0, 51))),
            cell02.renderRow(false, 5)));
    t.checkExpect(cell02.renderRow(false, 5),
        new BesideImage(
            cell02.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(153, 0, 102))),
            new EmptyImage()));

    t.checkExpect(cell10.renderRow(false, 5),
        new BesideImage(
            cell10.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(102, 0, 153))),
            cell11.renderRow(false, 5)));
    t.checkExpect(cell11.renderRow(false, 5),
        new BesideImage(
            cell11.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(153, 0, 102))),
            cell12.renderRow(false, 5)));
    t.checkExpect(cell12.renderRow(false, 5),
        new BesideImage(
            cell12.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(102, 0, 153))),
            new EmptyImage()));

    t.checkExpect(cell20.renderRow(false, 5),
        new BesideImage(
            cell20.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(50, 0, 204))),
            cell21.renderRow(false, 5)));
    t.checkExpect(cell21.renderRow(false, 5),
        new BesideImage(
            cell21.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 0, 255))),
            cell22.renderRow(false, 5)));
    t.checkExpect(cell22.renderRow(false, 5),
        new BesideImage(
            cell22.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(50, 0, 204))),
            new EmptyImage()));

    // reset gradient traversal
    cell00.traverse(0, new ArrayList<>(), true);

    // check lack of gradient after
    t.checkExpect(cell00.renderRow(false, 0),
        new BesideImage(cell00.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell01.renderRow(false, 0)));
    t.checkExpect(cell01.renderRow(false, 0),
        new BesideImage(cell01.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell02.renderRow(false, 0)));
    t.checkExpect(cell02.renderRow(false, 0),
        new BesideImage(cell02.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    t.checkExpect(cell10.renderRow(false, 0),
        new BesideImage(cell10.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell11.renderRow(false, 0)));
    t.checkExpect(cell11.renderRow(false, 0),
        new BesideImage(cell11.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell12.renderRow(false, 0)));
    t.checkExpect(cell12.renderRow(false, 0),
        new BesideImage(cell12.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    t.checkExpect(cell20.renderRow(false, 0),
        new BesideImage(cell20.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell21.renderRow(false, 0)));
    t.checkExpect(cell21.renderRow(false, 0),
        new BesideImage(cell21.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            cell22.renderRow(false, 0)));
    t.checkExpect(cell22.renderRow(false, 0),
        new BesideImage(cell22.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

  }

  void testSetVisited(Tester t) {
    this.initConds();

    // initialize cells
    Cell cell00 = new Cell();
    Cell cellWithEdges = new Cell(this.edge1, this.edge1, this.edge1, this.edge1);

    t.checkExpect(cell00.uniqueIncorrectMove(), true);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), true);

    // set visited to false, default
    cell00.setVisited(false);
    cellWithEdges.setVisited(false);

    // use uniqueIncorrectMove that observes the visited field
    // returns true since set correct path is false by default and we
    // set visited to false
    t.checkExpect(cell00.uniqueIncorrectMove(), true);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), true);

    // set visited to true for all
    cell00.setVisited(true);
    cellWithEdges.setVisited(true);
    // use uniqueIncorrectMove that observes the visited field from setVisited
    t.checkExpect(cell00.uniqueIncorrectMove(), false);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);

    // set correct path to true but visited to false
    cell00.setCorrectPath(true);
    cellWithEdges.setCorrectPath(true);
    cell00.setVisited(false);
    cellWithEdges.setVisited(false);
    // use uniqueIncorrectMove that observes the visited field from setVisited
    t.checkExpect(cell00.uniqueIncorrectMove(), false);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);

    // set all visited to true so correct path is also true
    cell00.setVisited(true);
    cellWithEdges.setVisited(true);
    t.checkExpect(cell00.uniqueIncorrectMove(), false);
    // use uniqueIncorrectMove that observes the visited field from setVisited
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);
  }

  void testSetCorrectPath(Tester t) {
    this.initConds();

    // initialize cells
    Cell cell00 = new Cell();
    Cell cellWithEdges = new Cell(this.edge1, this.edge1, this.edge1, this.edge1);

    t.checkExpect(cell00.uniqueIncorrectMove(), true);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), true);

    // set correct path to false, default
    cell00.setCorrectPath(false);
    cellWithEdges.setCorrectPath(false);

    // use uniqueIncorrectMove that observes the visited field
    // returns true since we set correct path to false and
    // visited field is false by default
    t.checkExpect(cell00.uniqueIncorrectMove(), true);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), true);

    // set visited to true for all
    cell00.setVisited(true);
    cellWithEdges.setVisited(true);

    // use uniqueIncorrectMove that observes the correct path field based on
    // changes from setCorrectPath
    t.checkExpect(cell00.uniqueIncorrectMove(), false);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);

    // set correct path to true but visited to false
    cell00.setCorrectPath(true);
    cellWithEdges.setCorrectPath(true);
    cell00.setVisited(false);
    cellWithEdges.setVisited(false);
    // use uniqueIncorrectMove that observes the correct path field based on
    // changes from setCorrectPath
    t.checkExpect(cell00.uniqueIncorrectMove(), false);
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);

    // set all visited to true so correct path is also true
    cell00.setVisited(true);
    cellWithEdges.setVisited(true);
    t.checkExpect(cell00.uniqueIncorrectMove(), false);

    // use uniqueIncorrectMove that observes the correct path field based on
    // changes from setCorrectPath
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), false);

    cellWithEdges.setVisited(false);
    cellWithEdges.setCorrectPath(false);
    // use observatory method
    t.checkExpect(cellWithEdges.uniqueIncorrectMove(), true);
  }

  void testUniqueIncorrectMove(Tester t) {
    this.initConds();

    // initialize other cells
    // we'll use this.cell1 for a default cell
    Cell cellWeightEdges = new Cell(new Edge(1.0), new Edge(3.0), new Edge(5.0), new Edge(7.0));

    // test with default fields before changing any of them via setCorrectPath or
    // setVisited
    t.checkExpect(this.cell1.uniqueIncorrectMove(), true);
    t.checkExpect(cellWeightEdges.uniqueIncorrectMove(), true);

    // test setVisited
    this.cell1.setVisited(true);
    cellWeightEdges.setVisited(true);
    t.checkExpect(this.cell1.uniqueIncorrectMove(), false);
    t.checkExpect(cellWeightEdges.uniqueIncorrectMove(), false);

    // test setCorrectPath with setVisited to true
    this.cell1.setCorrectPath(true);
    cellWeightEdges.setCorrectPath(true);
    t.checkExpect(this.cell1.uniqueIncorrectMove(), false);
    t.checkExpect(cellWeightEdges.uniqueIncorrectMove(), false);

    // make setVisited false only
    this.cell1.setVisited(false);
    cellWeightEdges.setVisited(false);
    t.checkExpect(this.cell1.uniqueIncorrectMove(), false);
    t.checkExpect(cellWeightEdges.uniqueIncorrectMove(), false);

    // set correct path and visited back to false and observe it with our method
    this.cell1.setCorrectPath(false);
    cellWeightEdges.setCorrectPath(false);
    t.checkExpect(this.cell1.uniqueIncorrectMove(), true);
    t.checkExpect(cellWeightEdges.uniqueIncorrectMove(), true);
  }

  void testSetSolution(Tester t) {
    this.initConds();

    Cell cell = new Cell();

    // check cell before
    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    // set solution field to true
    cell.setSolution(true);

    // check cell after
    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(
            cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))),
            new EmptyImage()));

  }

  void testSetPlayerIndicator(Tester t) {
    this.initConds();

    // check cell before
    Cell cell = new Cell();

    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    // set player indicator to true
    cell.setPlayerIndicator(true);

    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(
            cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176))),
            new EmptyImage()));

  }

  void testResetGradientCell(Tester t) {
    this.initConds();

    // check cell before
    Cell cell = new Cell();
    cell.setGradient(0);

    t.checkExpect(cell.renderRow(false, 1),
        new BesideImage(
            cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))),
            new EmptyImage()));

    // reset gradient
    cell.resetGradient();

    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

  }

  void testSetGradient(Tester t) {
    this.initConds();

    // check cell before
    Cell cell = new Cell();

    t.checkExpect(cell.renderRow(false, -1),
        new BesideImage(cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)),
            new EmptyImage()));

    // set gradient
    cell.setGradient(0);

    // check cell after
    t.checkExpect(cell.renderRow(false, 1),
        new BesideImage(
            cell.drawCell(new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))),
            new EmptyImage()));

  }

  void testSetStart(Tester t) {
    this.initConds();

    // check cell before
    Cell cell = new Cell();

    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new EmptyImage())))));

    // set start
    cell.setStart();

    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new EmptyImage()))))));
  }

  void testSetEnd(Tester t) {
    this.initConds();

    // check cell before
    Cell cell = new Cell();

    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new EmptyImage())))));

    // set end
    cell.setEnd();

    t.checkExpect(cell.drawCell(new EmptyImage()),
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new EmptyImage()))))));
  }

  void testSetStartGradient(Tester t) {
    this.initConds();

    // initialize the maze
    Maze maze = new Maze(2, 2, 420, false, 0);

    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // check lack of gradient before
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // set start gradient
    maze.setStartGradient();

    // update
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
            new Edge().render(false), 0, -4.5,
            new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                    new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(170, 0, 85)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 0, 255)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(85, 0, 170))))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // check for gradient
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));
  }

  void testSetExitGradient(Tester t) {
    this.initConds();

    // initialize the maze
    Maze maze = new Maze(2, 2, 420, false, 0);

    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // check lack of gradient before
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // set exit gradient
    maze.setExitGradient();

    // update
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
            new Edge().render(false), 0, -4.5,
            new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                    new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 0, 255))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(127, 0, 127)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(127, 0, 127)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // check for gradient
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));
  }

  void testResetGradientMaze(Tester t) {
    this.initConds();

    // initialize the maze
    Maze maze = new Maze(2, 2, 420, false, 0);

    // set exit gradient
    maze.setExitGradient();

    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
            new Edge().render(false), 0, -4.5,
            new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                    new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(0, 0, 255))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(127, 0, 127)))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(127, 0, 127)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(255, 0, 0))))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // make sure gradient applied
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // reset gradient
    maze.resetGradient();

    // update
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // check for lack of gradient
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));
  }

  void testRemoveWalls(Tester t) {
    this.initConds();

    // test big mazes
    Maze mazeBig = new Maze(100, 60);
    Maze mazeBigCopy = new Maze(100, 60);

    // check before
    t.checkExpect(mazeBig, mazeBigCopy);

    // remove walls
    mazeBig.removeWalls();
    // update copy
    mazeBigCopy.removeNWalls(100);

    // check after
    t.checkExpect(mazeBig, mazeBigCopy);

    // test med mazes
    Maze mazeMedium = new Maze(40, 20);
    Maze mazeMediumCopy = new Maze(40, 20);

    // check before
    t.checkExpect(mazeMedium, mazeMediumCopy);

    // remove walls
    mazeMedium.removeWalls();
    // update copy
    mazeMediumCopy.removeNWalls(10);

    // check after
    t.checkExpect(mazeMedium, mazeMediumCopy);

    // test small mazes
    Maze mazeSmall = new Maze(8, 4);
    Maze mazeSmallCopy = new Maze(8, 4);

    // check before
    t.checkExpect(mazeSmall, mazeSmallCopy);

    // remove walls
    mazeSmall.removeWalls();
    // update copy
    mazeSmallCopy.removeNWalls(3);

    // check after
    t.checkExpect(mazeSmall, mazeSmallCopy);

    // test tiny mazes
    Maze mazeTiny = new Maze(3, 3);
    Maze mazeTinyCopy = new Maze(3, 3);

    // check before
    t.checkExpect(mazeTiny, mazeTinyCopy);

    // remove walls
    mazeTiny.removeWalls();
    // update copy
    mazeTinyCopy.removeNWalls(1);

    // check after
    t.checkExpect(mazeTiny, mazeTinyCopy);
  }

  void testRemoveNWalls(Tester t) {
    this.initConds();

    Maze maze = new Maze(2, 2);

    // initialize a maze return image
    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
                new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // shouldnt do anything
    maze.removeNWalls(-1);

    // ensure they are still the same
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // shouldnt do anything
    maze.removeNWalls(0);

    // ensure they are still the same
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // remove one wall
    maze.removeNWalls(1);

    // update render
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure changes are reflected
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // remove 2 more walls
    maze.removeNWalls(2);

    // update render
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure changes are reflected
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

  }

  void testLabelCorrectCells(Tester t) {
    this.initConds();

    Maze maze = new Maze(2, 2, 420, false, 0);

    // label correct cells
    maze.buildReferencesPath();
    maze.labelCorrectCells();

    // make sure this is a correct move
    t.checkExpect(maze.attemptMove("right"), new Pair<Boolean, Integer>(true, 0));

    // make sure this is a correct move
    t.checkExpect(maze.attemptMove("down"), new Pair<Boolean, Integer>(false, 0));

    // make sure this is a incorrect move
    t.checkExpect(maze.attemptMove("left"), new Pair<Boolean, Integer>(true, 1));

  }

  void testInitMazeAuxiliaries(Tester t) {
    this.initConds();

    // initialize additional mazes
    Maze maze40by20 = new Maze(40, 20);
    Maze maze6by6 = new Maze(6, 6);

    // use initMazeAuxiliaries that will initialize the search fields
    maze40by20.initMazeAuxilaries();
    maze6by6.initMazeAuxilaries();

    // use observatory method that observes all fields
    t.checkExpect(maze40by20.updateSearch(false), new Pair<Boolean, Integer>(true, 0));
    t.checkExpect(maze40by20.updateSearch(true), new Pair<Boolean, Integer>(false, 0));
    t.checkExpect(maze6by6.updateSearch(false), new Pair<Boolean, Integer>(true, 0));
    t.checkExpect(maze6by6.updateSearch(true), new Pair<Boolean, Integer>(false, 0));
  }

  void testAttemptMove(Tester t) {
    this.initConds();

    // initialize maze
    Maze maze = new Maze(2, 2, 420, false, 0);
    maze.buildReferencesPath();
    maze.labelCorrectCells();

    // initialize render
    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // ensure maze is correct
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.attemptMove("right"), new Pair<Boolean, Integer>(true, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176)))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.attemptMove("down"), new Pair<Boolean, Integer>(false, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
            new Edge().render(false), 0, -4.5,
            new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                    new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))))))),
            new EmptyImage()));
    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.attemptMove("left"), new Pair<Boolean, Integer>(true, 1));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0, new OverlayOffsetImage(
            new Edge().render(false), 0, -4.5,
            new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                    new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(192, 169, 176)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0, new OverlayImage(
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(83, 221, 108))))))),
            new EmptyImage()));
    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));
  }

  void testBuildReferencesPath(Tester t) {
    this.initConds();

    Maze maze = new Maze(2, 2, 420, false, 0);
    // build the references
    maze.buildReferencesPath();

    // label correct moves using the reference path that was built
    maze.labelCorrectCells();

    // confirm the references were properly built
    // make sure this is a correct move
    t.checkExpect(maze.attemptMove("right"), new Pair<Boolean, Integer>(true, 0));

    // make sure this is a correct move
    t.checkExpect(maze.attemptMove("down"), new Pair<Boolean, Integer>(false, 0));

    // make sure this is a incorrect move
    t.checkExpect(maze.attemptMove("left"), new Pair<Boolean, Integer>(true, 1));
  }

  void testUpdateSearch(Tester t) {
    // bfs
    this.initConds();

    // initialize maze
    Maze maze = new Maze(2, 2, 420, false, 0);
    maze.buildReferencesPath();
    maze.labelCorrectCells();
    maze.initMazeAuxilaries();

    // initialize render
    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // ensure maze is correct
    t.checkExpect(maze.render(false, true, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.updateSearch(true), new Pair<Boolean, Integer>(true, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID,
                                new Color(151, 239, 233))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, true, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.updateSearch(true), new Pair<Boolean, Integer>(true, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID,
                                new Color(151, 239, 233))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));
    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, true, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // dfs
    this.initConds();

    // initialize maze
    maze = new Maze(2, 2, 420, false, 0);
    maze.buildReferencesPath();
    maze.labelCorrectCells();
    maze.initMazeAuxilaries();

    // initialize render
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure maze is correct
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.updateSearch(false), new Pair<Boolean, Integer>(true, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID,
                                new Color(151, 239, 233))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));

    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, true, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // make sure this returns correct package
    t.checkExpect(maze.updateSearch(false), new Pair<Boolean, Integer>(true, 0));

    // update render consts
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID,
                                new Color(151, 239, 233))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new EmptyImage(), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, new Color(151, 239, 233)))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));
    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    // ensure location changed
    t.checkExpect(maze.render(false, true, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));
  }

  void testRenderMaze(Tester t) {
    this.initConds();

    Maze maze = new Maze(2, 2, 420, false, 0);

    // initialize render
    WorldImage renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    WorldImage renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));

    WorldImage startNodeRender = new AboveImage(renderRow1,
        new AboveImage(renderRow2, new EmptyImage()));

    // test case: all booleans false
    t.checkExpect(maze.render(false, false, 0, false, 0, 0),
        new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender));

    // test case: userWon true
    t.checkExpect(maze.render(true, false, 0, false, 0, 0),
        new AboveImage(
            new TextImage("Congrats you won! Press r (or any other mode toggle) to reset.", 20,
                Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender)));

    // test case: showAlgoComp true, -1's
    t.checkExpect(maze.render(false, false, 0, true, -1, -1),
        new AboveImage(
            new TextImage("Finish DFS and BFS to see performance comparison!", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender)));

    // test case: showAlgoComp true, equal
    t.checkExpect(maze.render(false, false, 0, true, 0, 0),
        new AboveImage(new TextImage("DFS and BFS had the same number of moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender)));

    // test case: showAlgoComp true, DFS > BFS
    t.checkExpect(maze.render(false, false, 0, true, 10, 5),
        new AboveImage(new TextImage("BFS had 5 fewer moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender)));

    // test case: showAlgoComp true, BFS > DFS
    t.checkExpect(maze.render(false, false, 0, true, 5, 10),
        new AboveImage(new TextImage("DFS had 5 fewer moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender)));

    // test case: showAlgoComp and userWon true
    t.checkExpect(maze.render(true, false, 0, true, 5, 10), new AboveImage(
        new TextImage("Congrats you won! Press r (or any other mode toggle) to reset.", 20,
            Color.black),
        new AboveImage(new TextImage("DFS had 5 fewer moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender))));

    // test case: all booleans true, but no cells visited yet
    t.checkExpect(maze.render(true, true, 0, true, 5, 10), new AboveImage(
        new TextImage("Congrats you won! Press r (or any other mode toggle) to reset.", 20,
            Color.black),
        new AboveImage(new TextImage("DFS had 5 fewer moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender))));

    // test case: all booleans true, but cells have been visited

    // update search
    maze.initMazeAuxilaries();
    maze.updateSearch(true);

    // update renders
    renderRow1 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(19, 80, 91)),
                            new RectangleImage(10, 10, OutlineMode.SOLID,
                                new Color(151, 239, 233))))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0,
                new OverlayOffsetImage(new EmptyImage(), 0, -4.5,
                    new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                        new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
            new EmptyImage()));
    renderRow2 = new BesideImage(
        new OverlayOffsetImage(new Edge().render(true), 4.5, 0,
            new OverlayOffsetImage(new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new Edge().render(false), 0, 4.5,
                    new OverlayOffsetImage(new EmptyImage(), -4.5, 0,
                        new RectangleImage(10, 10, OutlineMode.SOLID, Color.white))))),
        new BesideImage(
            new OverlayOffsetImage(new EmptyImage(), 4.5, 0, new OverlayOffsetImage(
                new Edge().render(false), 0, -4.5,
                new OverlayOffsetImage(new EmptyImage(), 0, 4.5,
                    new OverlayOffsetImage(new Edge().render(true), -4.5, 0,
                        new OverlayImage(
                            new RectangleImage(10, 10, OutlineMode.SOLID, new Color(207, 207, 234)),
                            new RectangleImage(10, 10, OutlineMode.SOLID, Color.white)))))),
            new EmptyImage()));
    startNodeRender = new AboveImage(renderRow1, new AboveImage(renderRow2, new EmptyImage()));

    t.checkExpect(maze.render(true, true, 0, true, 5, 10), new AboveImage(
        new TextImage("Congrats you won! Press r (or any other mode toggle) to reset.", 20,
            Color.black),
        new AboveImage(new TextImage("DFS had 5 fewer moves", 20, Color.black),
            new AboveImage(new TextImage("Wrong Moves: " + 0, 20, Color.black), startNodeRender))));

  }

  void testCreateMinimumSpanning(Tester t) {
    this.initConds();

    // observational paths
    // createMinimumSpanning -> find -> test values
    // createMinimumSpanning -> connectedEdges -> removeWalls

    // initialize an additional maze and a nodeGrid
    Maze maze40by20 = new Maze(40, 20);
    ArrayList<ArrayList<Cell>> nodeGrid = new ArrayList<>();
    // adds 4 rows of cell lists
    for (int rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      nodeGrid.add(new ArrayList<>());
    }

    // adds 4 columns to each row 4 times
    for (int rowIndex = 0; rowIndex < 4; rowIndex += 1) {
      // adds 4 columns to this row
      nodeGrid.get(rowIndex).add(this.cellH1);
      nodeGrid.get(rowIndex).add(this.cellH2);
      nodeGrid.get(rowIndex).add(this.cellH3);
      nodeGrid.get(rowIndex).add(this.cellH4);
    }

    // test before calling createMinimumSpanning
    t.checkExpect(this.maze1.find(this.rep, this.cellH1), this.cellH1);
    t.checkExpect(this.maze1.find(this.rep, this.cellH2), this.cellH1);
    t.checkExpect(maze40by20.find(this.rep, this.cellH3), this.cellH1);
    t.checkExpect(maze40by20.find(this.rep, this.cellH4), this.cellH4);
    t.checkExpect(this.maze1.removeWalls(), true);
    t.checkExpect(maze40by20.removeWalls(), true);

    // in order for createMinimumSpanning to work, linkGrid must be called
    this.maze1.linkGrid(nodeGrid, new Random(1), 0);
    maze40by20.linkGrid(nodeGrid, new Random(100), 10);

    // call create createMinimumSpanning
    // this won't call connectedEdges
    new Maze(4, 4).createMinimumSpanning(nodeGrid, false);
    // this will call connectedEdges
    maze40by20.createMinimumSpanning(nodeGrid, true);
    // check after
    t.checkExpect(this.maze1.removeWalls(), true);
    t.checkExpect(maze40by20.removeWalls(), true);
    t.checkExpect(maze40by20.find(this.rep, this.cellH3), this.cellH1);
    t.checkExpect(maze40by20.find(this.rep, this.cellH4), this.cellH4);
    t.checkExpect(this.maze1.find(this.rep, this.cellH1), this.cellH1);
    t.checkExpect(this.maze1.find(this.rep, this.cellH2), this.cellH1);
  }

  void testRGSForConstruction(Tester t) {
    this.initConds();

    // make a test scene for all the games
    WorldScene expectedScene = new WorldScene(1000, 1000);

    // scene before calling resetGameStatesForConstruction
    // reset scene before placing image and testing
    expectedScene.placeImageXY(new Maze(100, 60).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.game.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(40, 25, 1).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameHorizontal.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(40, 25, 2).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameVertical.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(this.mazeTiny.render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameTiny.makeScene(), expectedScene);

    // make changes to the game's scene via maze
    expectedScene = new WorldScene(1000, 1000);
    Maze affectedMaze = new Maze(3, 3);
    // initialize the cell grid
    ArrayList<ArrayList<Cell>> gridCell = new ArrayList<>();
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());
    gridCell.add(new ArrayList<>());

    Cell cell00 = new Cell();
    Cell cell01 = new Cell();
    Cell cell02 = new Cell();

    Cell cell10 = new Cell();
    Cell cell11 = new Cell();
    Cell cell12 = new Cell();

    Cell cell20 = new Cell();
    Cell cell21 = new Cell();
    Cell cell22 = new Cell();

    gridCell.get(0).add(cell00);
    gridCell.get(0).add(cell01);
    gridCell.get(0).add(cell02);

    gridCell.get(1).add(cell10);
    gridCell.get(1).add(cell11);
    gridCell.get(1).add(cell12);

    gridCell.get(2).add(cell20);
    gridCell.get(2).add(cell21);
    gridCell.get(2).add(cell22);

    affectedMaze.linkGrid(gridCell, new Random(420), 0);
    affectedMaze.createMinimumSpanning(gridCell, true);

    MazeSolver anotherGame = new MazeSolver(affectedMaze);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(3, 3).render(true, true, 1, true, 9, 99), 500, 500);
    t.checkFail(anotherGame.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(3, 3).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(anotherGame.makeScene(), expectedScene);

    // call resetGameStatesForConstruction to make the changes
    this.game.resetGameStatesForConstruction();
    this.gameHorizontal.resetGameStatesForConstruction();
    this.gameVertical.resetGameStatesForConstruction();
    this.gameTiny.resetGameStatesForConstruction();
    anotherGame.resetGameStatesForConstruction();

    // since the games are reset, they should be tested with their default scenes
    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(100, 60).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.game.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(40, 25, 1).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameHorizontal.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(new Maze(40, 25, 2).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameVertical.makeScene(), expectedScene);

    expectedScene = new WorldScene(1000, 1000);
    expectedScene.placeImageXY(this.mazeTiny.render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameTiny.makeScene(), expectedScene);
  }

  // testing resetGameStatesForSearch
  void testRGSForSearch(Tester t) {
    this.initConds();

    Maze tinyMaze = new Maze(2, 2, 420, false, 0);
    this.gameTiny = new MazeSolver(tinyMaze);

    // set up prev world scene
    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(tinyMaze.render(false, true, 0, false, 0, 0), 500, 500);

    // test before
    t.checkExpect(this.gameTiny.makeScene(), w);

    // search tiny maze
    tinyMaze.initMazeAuxilaries();
    tinyMaze.updateSearch(true);

    // reset game state
    this.gameTiny.resetGameStatesForSearch();

    // test after to see if its back to original state
    t.checkExpect(this.gameTiny.makeScene(), w);

  }

  void testMakeScene(Tester t) {
    this.initConds();

    // default canvas with our constant dimensions
    WorldScene defaultScene;

    // test makeScene with various maze games
    // tiny game
    defaultScene = new WorldScene(1000, 1000);
    defaultScene.placeImageXY(this.mazeTiny.render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameTiny.makeScene(), defaultScene);

    // regular large game
    defaultScene = new WorldScene(1000, 1000);
    defaultScene.placeImageXY(new Maze(100, 60).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.game.makeScene(), defaultScene);

    // horizontal game
    defaultScene = new WorldScene(1000, 1000);
    defaultScene.placeImageXY(new Maze(40, 25, 1).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameHorizontal.makeScene(), defaultScene);

    // vertical game
    defaultScene = new WorldScene(1000, 1000);
    defaultScene.placeImageXY(new Maze(40, 25, 2).render(false, true, 0, false, -1, -1), 500, 500);
    t.checkExpect(this.gameVertical.makeScene(), defaultScene);
  }

  void testSetUpMazeForSearch(Tester t) {
    this.initConds();

    Maze tinyMaze = new Maze(2, 2, 420, false, 0);
    this.gameTiny = new MazeSolver(tinyMaze);

    // set up prev world scene
    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(tinyMaze.render(false, true, 0, false, 0, 0), 500, 500);

    // test before
    t.checkExpect(this.gameTiny.makeScene(), w);

    // search tiny maze
    tinyMaze.initMazeAuxilaries();
    tinyMaze.updateSearch(true);

    // set up maze for search
    this.gameTiny.setUpMazeForSearch();

    // test after to see if its back to original state
    t.checkExpect(this.gameTiny.makeScene(), w);
  }

  void testOnKeyEvent(Tester t) {
    this.initConds();

    Maze maze = new Maze(2, 2, 420, false, 0);
    Maze maze2 = new Maze(2, 2, 420, false, 0);

    maze.initMazeAuxilaries();
    maze2.initMazeAuxilaries();

    MazeSolver mazeSolver = new MazeSolver(maze);
    MazeSolver mazeSolver2 = new MazeSolver(maze2);

    // make sure maze and its solvers refer to their copies
    t.checkExpect(maze, maze2);
    t.checkExpect(mazeSolver, mazeSolver2);

    // press a (invalid keypress)
    mazeSolver.onKeyEvent("a");

    // should still refer to copy
    t.checkExpect(mazeSolver, mazeSolver2);

    mazeSolver.onKeyEvent("right");
    // should still refer to copy (not in manual mode)
    t.checkExpect(mazeSolver, mazeSolver2);
    mazeSolver.onKeyEvent("left");
    // should still refer to copy (not in manual mode)
    t.checkExpect(mazeSolver, mazeSolver2);
    mazeSolver.onKeyEvent("down");
    // should still refer to copy (not in manual mode)
    t.checkExpect(mazeSolver, mazeSolver2);
    mazeSolver.onKeyEvent("up");
    // should still refer to copy (not in manual mode)
    t.checkExpect(mazeSolver, mazeSolver2);

    // press u
    mazeSolver.onKeyEvent("u");
    mazeSolver2.onKeyEvent("u");

    mazeSolver.onTick();

    // make sure it STILL refers to its copy after using key "u"
    t.checkExpect(mazeSolver, mazeSolver2);

    // press b
    mazeSolver.onKeyEvent("b");

    // set up search
    mazeSolver.resetGameStatesForSearch();
    mazeSolver.setUpMazeForSearch();

    // let game tick
    mazeSolver.onTick();

    // render b
    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 1, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press d
    mazeSolver.onKeyEvent("d");

    // set up search
    mazeSolver.resetGameStatesForSearch();
    mazeSolver.setUpMazeForSearch();

    // let game tick
    mazeSolver.onTick();

    // render d
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 1, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press r
    mazeSolver.onKeyEvent("r");

    maze = new Maze(2, 2);

    // render r
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press 0
    mazeSolver.onKeyEvent("0");

    maze = new Maze(2, 2, 0);

    // render 0
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press 1
    mazeSolver.onKeyEvent("1");

    maze = new Maze(2, 2, 1);

    // render 1
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press 2
    mazeSolver.onKeyEvent("2");

    maze = new Maze(2, 2, 2);

    // render 2
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press m
    mazeSolver.onKeyEvent("m");
    maze = new Maze(2, 2, 2);

    // render m
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press left
    mazeSolver.onKeyEvent("left");

    // render left
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press right
    mazeSolver.onKeyEvent("right");

    // render right
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press down
    mazeSolver.onKeyEvent("down");

    // render down
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press up
    mazeSolver.onKeyEvent("up");

    // render up
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press v
    mazeSolver.onKeyEvent("v");

    // render v
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press v
    mazeSolver.onKeyEvent("s");

    // render s
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press e
    mazeSolver.onKeyEvent("e");

    // render e
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);

    // press p
    mazeSolver.onKeyEvent("p");

    // render p
    w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 0, false, 0, 0), 500, 500);
    t.checkExpect(mazeSolver.makeScene(), w);
  }

  void testOnTick(Tester t) {
    this.initConds();

    // init maze solver copies
    Maze maze = new Maze(2, 2, 420, false, 0);
    Maze maze2 = new Maze(2, 2, 420, false, 0);

    maze.initMazeAuxilaries();
    maze2.initMazeAuxilaries();

    MazeSolver mazeSolver = new MazeSolver(maze);
    MazeSolver mazeSolver2 = new MazeSolver(maze2);

    // case not under construction, not dfs active, not bfs active

    // make sure it refers to its copy
    t.checkExpect(mazeSolver, mazeSolver2);

    // make it not under construction
    mazeSolver.onKeyEvent("u");
    mazeSolver2.onKeyEvent("u");

    // let the game tick
    mazeSolver.onTick();

    // make sure it STILL refers to its copy
    t.checkExpect(mazeSolver, mazeSolver2);

    // make it dfs active
    mazeSolver.onKeyEvent("d");

    // tick
    mazeSolver.onTick();

    WorldScene w = new WorldScene(1000, 1000);
    w.placeImageXY(maze.render(false, true, 1, false, 0, 0), 500, 500);

    t.checkExpect(mazeSolver.makeScene(), w);

    // set up for bfs active case
    Maze maze3 = new Maze(2, 2, 420, false, 0);
    Maze maze4 = new Maze(2, 2, 420, false, 0);

    maze3.initMazeAuxilaries();
    maze4.initMazeAuxilaries();

    MazeSolver mazeSolver3 = new MazeSolver(maze3);
    MazeSolver mazeSolver4 = new MazeSolver(maze4);

    // case not under construction, not dfs active, not bfs active

    // make sure it refers to its copy
    t.checkExpect(mazeSolver3, mazeSolver4);

    // make it not under construction
    mazeSolver3.onKeyEvent("u");
    mazeSolver4.onKeyEvent("u");

    // make it bfs active
    mazeSolver3.onKeyEvent("b");

    // let the game tick
    mazeSolver3.onTick();

    WorldScene w2 = new WorldScene(1000, 1000);
    w2.placeImageXY(maze.render(false, true, 1, false, 0, 0), 500, 500);

    t.checkExpect(mazeSolver3.makeScene(), w);
  }

}
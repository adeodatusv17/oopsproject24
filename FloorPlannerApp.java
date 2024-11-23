import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;

public class FloorPlannerApp extends JFrame {
    private FloorPlan floorPlan;
    private final JFileChooser fileChooser = new JFileChooser();
    private File currentFile = null;

    public FloorPlannerApp() {
        this.setTitle("2D Floor Planner ");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);

        floorPlan = new FloorPlan();

        // add menu bar
        setJMenuBar(createMenuBar());

        // add room type
        JPanel roomLegendPanel = createRoomLegendPanel();
        this.add(roomLegendPanel, BorderLayout.WEST);

        this.add(floorPlan, BorderLayout.CENTER);
        this.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newFileItem = new JMenuItem("New");
        newFileItem.addActionListener(e -> floorPlan.clear());
        JMenuItem saveFileItem = new JMenuItem("Save");
        saveFileItem.addActionListener(e -> {
            if (currentFile != null) {
                floorPlan.savePlan(currentFile);
            } else {
                saveAsFile();
            }
        });
        JMenuItem saveAsFileItem = new JMenuItem("Save As");
        saveAsFileItem.addActionListener(e -> saveAsFile());
        JMenuItem openFileItem = new JMenuItem("Open");
        openFileItem.addActionListener(e -> openPlanFromFile());
        JMenuItem exitFileItem = new JMenuItem("Exit");
        exitFileItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.addSeparator();
        fileMenu.add(saveFileItem);
        fileMenu.add(saveAsFileItem);
        fileMenu.addSeparator();
        fileMenu.add(exitFileItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem deleteItem = new JMenuItem("Delete");
        JMenuItem selectAllItem = new JMenuItem("Select All");

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);
        editMenu.add(selectAllItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        JMenuItem setGridSizeItem = new JMenuItem("Set Grid Size");
        setGridSizeItem.addActionListener(e -> floorPlan.setGridSize());
        JCheckBoxMenuItem gridLinesItem = new JCheckBoxMenuItem("Show Grid Lines", true);
        JCheckBoxMenuItem labelsItem = new JCheckBoxMenuItem("Show Room Labels", true);
        JCheckBoxMenuItem dimensionsItem = new JCheckBoxMenuItem("Show Dimensions", true);
        JCheckBoxMenuItem snappingItem = new JCheckBoxMenuItem("Snap to Grid", true);

        labelsItem.addActionListener(e ->floorPlan.toggleRoomLabels());
        gridLinesItem.addActionListener(e -> floorPlan.toggleGridLines(gridLinesItem.isSelected()));
        snappingItem.addActionListener(e -> floorPlan.setSnapToGrid(snappingItem.isSelected()));


        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.addSeparator();
        viewMenu.add(setGridSizeItem);
        viewMenu.add(gridLinesItem);
        viewMenu.add(labelsItem);
        viewMenu.add(dimensionsItem);
        viewMenu.add(snappingItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }

    private void saveAsFile() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            floorPlan.savePlan(currentFile);
        }
    }



    private void openPlanFromFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            if (currentFile != null) {
                floorPlan.loadPlan(currentFile);
            } else {
                JOptionPane.showMessageDialog(this, "No file selected.",
                        "Open File", JOptionPane.WARNING_MESSAGE);
            }
        }
    }



    //resize functionality
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private JPanel createRoomLegendPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(200, 600));

        // define the common preferred height for all panels
        int panelHeight = 120;  // Set height for each panel

        // room panel
        JPanel roomsPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        roomsPanel.setBorder(BorderFactory.createTitledBorder("Add Rooms"));
        roomsPanel.setPreferredSize(new Dimension(120, panelHeight));  // Adjust height for consistency

        String[] roomTypes = {"Bedroom", "Bathroom", "Kitchen", "Living Room", "Dining Room"};
        int roomWidth = 150;
        int roomHeight = 100;

        for (String roomType : roomTypes) {
            JButton button = new JButton(roomType);
            button.addActionListener(e -> {
                int startRow = 0;
                int startCol = 0;
                boolean placed = false;

                while (!placed) {
                    int x = startCol * floorPlan.getGridSize();
                    int y = startRow * floorPlan.getGridSize();

                    Rectangle newBounds = new Rectangle(x, y, roomWidth, roomHeight);
                    Room tempRoom = new Room(newBounds.x, newBounds.y, newBounds.width, newBounds.height, roomType);

                    if (!floorPlan.overlapCheck(tempRoom)) {
                        floorPlan.addRoom(tempRoom);
                        placed = true;
                    } else {
                        startCol++;
                        if (x + roomWidth > floorPlan.getWidth()) {
                            startCol = 0;
                            startRow++;
                        }
                        if (y + roomHeight > floorPlan.getHeight()) {
                            System.out.println("No space left for the room.");
                            break;
                        }
                    }
                }
                floorPlan.repaint();
            });
            roomsPanel.add(button);
        }

        // fixtures panel
        JPanel fixturesPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        fixturesPanel.setBorder(BorderFactory.createTitledBorder("Add Fixtures"));
        fixturesPanel.setPreferredSize(new Dimension(120, panelHeight));  // Adjust height for consistency

        String[] fixtureTypes = {"Toilet", "Shower", "Kitchen Sink", "WashBasin", "Stove"};
        String[] fixtureImagePaths = {
                "C:/Users/Shashwat Singh/Desktop/fixtures/toilet.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/shower.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/kitchen.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/basin.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/gas-stove.png"
        };

        for (int i = 0; i < fixtureTypes.length; i++) {
            String fixtureType = fixtureTypes[i];

            JButton fixtureButton = new JButton(fixtureType);
            fixtureButton.setHorizontalTextPosition(SwingConstants.CENTER);
            fixtureButton.setVerticalTextPosition(SwingConstants.BOTTOM);

            final int index = i;
            fixtureButton.addActionListener(e -> {
                floorPlan.addItem(floorPlan, fixtureTypes[index], "fixture");
            });

            fixturesPanel.add(fixtureButton);
        }


        //furniture panel
        JPanel furniturePanel = new JPanel(new GridLayout(5, 1, 10, 10));
        furniturePanel.setBorder(BorderFactory.createTitledBorder("Add Furniture"));
        furniturePanel.setPreferredSize(new Dimension(120, panelHeight));

        String[] furnitureTypes = {"Bed", "Table", "Chair", "Sofa", "Dining Set"};
        String[] furnitureImagePaths = {

                "C:/Users/Shashwat Singh/Desktop/fixtures/bed.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/table.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/chair.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/sofa.png",
                "C:/Users/Shashwat Singh/Desktop/fixtures/dining-table.png"
        };
        for (int i = 0; i < furnitureTypes.length; i++) {
            String furnitureType = furnitureTypes[i];

            JButton furnitureButton = new JButton(furnitureType);
            furnitureButton.setHorizontalTextPosition(SwingConstants.CENTER);
            furnitureButton.setVerticalTextPosition(SwingConstants.BOTTOM);

            final int index = i;
            furnitureButton.addActionListener(e -> {
                floorPlan.addItem(floorPlan, furnitureTypes[index], "furniture");
            });

            furniturePanel.add(furnitureButton);
        }


        // add all panels to the controlPanel
        controlPanel.add(roomsPanel);
        controlPanel.add(fixturesPanel);
        controlPanel.add(furniturePanel);
        controlPanel.add(createDoorWindowPanel());

        return controlPanel;
    }


    private JPanel createDoorWindowPanel() {
        JPanel doorWindowPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        doorWindowPanel.setBorder(BorderFactory.createTitledBorder("Add Doors & Windows"));
        doorWindowPanel.setPreferredSize(new Dimension(120, 120));

        // Door to outside button
        ImageIcon doorIcon = new ImageIcon("path/to/door-icon.png"); // Add appropriate icon path
        doorIcon = resizeIcon(doorIcon, 20, 20);
        JButton exteriorDoorButton = new JButton("Exterior Door", doorIcon);
        exteriorDoorButton.setHorizontalTextPosition(SwingConstants.CENTER);
        exteriorDoorButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        exteriorDoorButton.addActionListener(e -> addExteriorDoor());

        // Door between rooms button
        ImageIcon interiorDoorIcon = new ImageIcon("path/to/interior-door-icon.png"); // Add appropriate icon path
        interiorDoorIcon = resizeIcon(interiorDoorIcon, 20, 20);
        JButton interiorDoorButton = new JButton("Interior Door", interiorDoorIcon);
        interiorDoorButton.setHorizontalTextPosition(SwingConstants.CENTER);
        interiorDoorButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        interiorDoorButton.addActionListener(e -> addInteriorDoor());

        // Window button
        ImageIcon windowIcon = new ImageIcon("path/to/window-icon.png"); // Add appropriate icon path
        windowIcon = resizeIcon(windowIcon, 20, 20);
        JButton windowButton = new JButton("Window", windowIcon);
        windowButton.setHorizontalTextPosition(SwingConstants.CENTER);
        windowButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        windowButton.addActionListener(e -> addWindow());

        doorWindowPanel.add(exteriorDoorButton);
        doorWindowPanel.add(interiorDoorButton);
        doorWindowPanel.add(windowButton);

        return doorWindowPanel;
    }

    private void addExteriorDoor() {
        floorPlan.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        floorPlan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Room selectedRoom = null;
                String wallPosition = null;
                Point clickPoint = e.getPoint();

                // Find the clicked room and wall
                for (Component comp : floorPlan.getComponents()) {
                    if (comp instanceof Room) {
                        Room room = (Room) comp;
                        Rectangle bounds = room.getBounds();
                        wallPosition = getWallPosition(clickPoint, bounds);

                        if (wallPosition != null) {
                            selectedRoom = room;
                            break;
                        }
                    }
                }

                if (selectedRoom != null && wallPosition != null) {
                    floorPlan.addDoor(clickPoint, wallPosition, selectedRoom, null);
                }

                // Reset cursor and remove listener
                floorPlan.setCursor(Cursor.getDefaultCursor());
                floorPlan.removeMouseListener(this);
            }
        });
    }

    private void addInteriorDoor() {
        floorPlan.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        final Room[] selectedRooms = new Room[2];
        final Point[] clickPoints = new Point[2];
        final int[] clicks = {0};

        MouseAdapter doorAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickPoint = e.getPoint();
                Room clickedRoom = null;
                String wallPosition = null;

                for (Component comp : floorPlan.getComponents()) {
                    if (comp instanceof Room) {
                        Room room = (Room) comp;

                        if (clicks[0] == 1 && room == selectedRooms[0]) {
                            continue;
                        }

                        Rectangle bounds = room.getBounds();
                        wallPosition = getWallPosition(clickPoint, bounds);

                        if (wallPosition != null) {
                            clickedRoom = room;
                            break;
                        }
                    }
                }

                if (clickedRoom != null && wallPosition != null) {
                    if (clicks[0] == 1 && clickedRoom == selectedRooms[0]) {
                        JOptionPane.showMessageDialog(floorPlan,
                                "Please select a different room for the door.");
                        return;
                    }

                    selectedRooms[clicks[0]] = clickedRoom;
                    clickPoints[clicks[0]] = clickPoint;
                    clicks[0]++;

                    if (clicks[0] == 2) {
                        System.out.println("First room: " + selectedRooms[0].getBounds());
                        System.out.println("Second room: " + selectedRooms[1].getBounds());
                        System.out.println("First click: " + clickPoints[0]);
                        System.out.println("Second click: " + clickPoints[1]);
                        System.out.println("First wall: " + getWallPosition(clickPoints[0], selectedRooms[0].getBounds()));
                        System.out.println("Second wall: " + getWallPosition(clickPoints[1], selectedRooms[1].getBounds()));

                        if (areRoomsAdjacent(selectedRooms[0], selectedRooms[1])) {
                            floorPlan.addDoor(clickPoints[0], getWallPosition(clickPoints[0],
                                    selectedRooms[0].getBounds()), selectedRooms[0], selectedRooms[1]);
                        } else {
                            JOptionPane.showMessageDialog(floorPlan,
                                    "Selected rooms must be adjacent to add a door.");
                        }

                        // Reset cursor and remove listener
                        floorPlan.setCursor(Cursor.getDefaultCursor());
                        floorPlan.removeMouseListener(this);
                        clicks[0] = 0;
                    }
                }
            }
        };

        floorPlan.addMouseListener(doorAdapter);
    }

    private void addWindow() {
        floorPlan.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        floorPlan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickPoint = e.getPoint();
                Room selectedRoom = null;
                String wallPosition = null;


                for (Component comp : floorPlan.getComponents()) {  // find clicked room and wall
                    if (comp instanceof Room) {
                        Room room = (Room) comp;
                        Rectangle bounds = room.getBounds();
                        wallPosition = getWallPosition(clickPoint, bounds);

                        if (wallPosition != null) {
                            selectedRoom = room;
                            break;
                        }
                    }
                }

                if (selectedRoom != null && wallPosition != null) {
                    if (!floorPlan.isValidWindowPlacement(clickPoint, wallPosition, selectedRoom)) {
                        JOptionPane.showMessageDialog(floorPlan,
                                "Invalid window placement. Windows cannot be placed between rooms.",
                                "Error", JOptionPane.WARNING_MESSAGE);

                    }
                    else{
                    floorPlan.addWindow(clickPoint, wallPosition, selectedRoom);
                }}

                // reset cursor and remove listener
                floorPlan.setCursor(Cursor.getDefaultCursor());
                floorPlan.removeMouseListener(this);
            }
        });
    }

    private String getWallPosition(Point clickPoint, Rectangle bounds) {
        int tolerance = 100; //tolerance


        if (Math.abs(clickPoint.y - bounds.y) <= tolerance &&
                clickPoint.x >= bounds.x && clickPoint.x <= bounds.x + bounds.width) {
            return "north";
        }
        if (Math.abs(clickPoint.y - (bounds.y + bounds.height)) <= tolerance &&
                clickPoint.x >= bounds.x && clickPoint.x <= bounds.x + bounds.width) {
            return "south";
        }
        if (Math.abs(clickPoint.x - bounds.x) <= tolerance &&
                clickPoint.y >= bounds.y && clickPoint.y <= bounds.y + bounds.height) {
            return "west";
        }
        if (Math.abs(clickPoint.x - (bounds.x + bounds.width)) <= tolerance &&
                clickPoint.y >= bounds.y && clickPoint.y <= bounds.y + bounds.height) {
            return "east";
        }
        return null;
    }

    private boolean areRoomsAdjacent(Room room1, Room room2) {

        Rectangle r1 = room1.getBounds();
        Rectangle r2 = room2.getBounds();
        int tolerance = 10;

        int verticalOverlap = Math.min(r1.y + r1.height, r2.y + r2.height) - Math.max(r1.y, r2.y);
        int horizontalOverlap = Math.min(r1.x + r1.width, r2.x + r2.width) - Math.max(r1.x, r2.x);
        System.out.println("Room1: " + r1);
        System.out.println("Room2: " + r2);
        System.out.println("Vertical overlap: " + verticalOverlap);
        System.out.println("Horizontal overlap: " + horizontalOverlap);

        int minOverlap = 5; // minimum  overlap required

        boolean shareVerticalWall =
                (Math.abs((r1.x + r1.width) - r2.x) <= tolerance && verticalOverlap >= minOverlap) ||
                        (Math.abs((r2.x + r2.width) - r1.x) <= tolerance && verticalOverlap >= minOverlap);

        boolean shareHorizontalWall =
                (Math.abs((r1.y + r1.height) - r2.y) <= tolerance && horizontalOverlap >= minOverlap) ||
                        // Room1's top wall near Room2's bottom wall
                        (Math.abs((r2.y + r2.height) - r1.y) <= tolerance && horizontalOverlap >= minOverlap);

        return shareVerticalWall || shareHorizontalWall;
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            FloorPlannerApp app = new FloorPlannerApp();
            Image icon = Toolkit.getDefaultToolkit().getImage("C:/Users/Shashwat Singh/Desktop/images.png");
            app.setIconImage(icon);
            app.setExtendedState(JFrame.MAXIMIZED_BOTH);
            if (args.length > 0) {
                File file = new File(args[0]);
                if (file.exists()) {
                    app.floorPlan.loadPlan(file);
                }
            }
        });
    }


}

class FloorPlan extends JPanel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private transient Room selectedRoom = null;
    private final ArrayList<Room> rooms;
    private boolean showGridLines = true;
    private static int WALL_THICKNESS = 7;
    private int gridSize = 20;
    private boolean snapToGrid = true;
    private boolean isDragging = false;
    private Color wallColor = new Color(25, 25, 25);
    String[] positions = {"north", "south", "east", "west"};
    String[] alignments = {"left", "center", "right"};
    private ArrayList<WallOpening> wallOpenings = new ArrayList<>();
    private static final int MIN_OPENING_DISTANCE = 50;
    private static final float[] WINDOW_DASH_PATTERN = {5.0f, 5.0f};


    public void addDoor(Point location, String position, Room parentRoom, Room connectedRoom) {
        // Check if door placement is valid
        if (!isValidDoorPlacement(location, position, parentRoom, connectedRoom)) {
            JOptionPane.showMessageDialog(this, "Invalid door placement. Check room types and existing openings.");
            return;
        }

        WallOpening door = new WallOpening(WallOpening.Type.DOOR, location, position, parentRoom, connectedRoom);
        wallOpenings.add(door);
        repaint();
    }


    public void addWindow(Point location, String position, Room parentRoom) {

        if (!isValidWindowPlacement(location, position, parentRoom)) {//check validity of window placement
            JOptionPane.showMessageDialog(this, "Invalid window placement. Windows cannot be between rooms.");
            return;
        }
        WallOpening window = new WallOpening(WallOpening.Type.WINDOW, location, position, parentRoom, null);
        wallOpenings.add(window);
        repaint();
    }







    private boolean isValidDoorPlacement(Point location, String position, Room parentRoom, Room connectedRoom) {
        // Check if bedroom or bathroom connecting to outside
        if (connectedRoom == null &&
                (parentRoom.roomType.equals("Bedroom") || parentRoom.roomType.equals("Bathroom"))) {
            return false;
        }

        // Check for overlap with existing openings
        return !hasOverlappingOpenings(location, position, parentRoom);
    }


    boolean isValidWindowPlacement(Point location, String position, Room parentRoom) {
        // Check if window is between rooms
        for (Room room : rooms) {
            if (room != parentRoom && areWallsAdjacent(parentRoom, room, position)) {
                return false;
            }
        }

        // Check for overlap with existing openings
        return !hasOverlappingOpenings(location, position, parentRoom);
    }


    private boolean hasOverlappingOpenings(Point location, String position, Room parentRoom) {
        for (WallOpening opening : wallOpenings) {
            if (opening.getParentRoom() == parentRoom &&
                    opening.getPosition().equals(position)) {
                // Check distance between openings
                double distance = position.equals("north") || position.equals("south") ?
                        Math.abs(location.x - opening.getLocation().x) :
                        Math.abs(location.y - opening.getLocation().y);

                if (distance < MIN_OPENING_DISTANCE) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean areWallsAdjacent(Room room1, Room room2, String position) {
        Rectangle r1 = room1.getBounds();
        Rectangle r2 = room2.getBounds();
        int tolerance =10;

        switch (position) {
            case "north":
                return Math.abs(r1.y - (r2.y + r2.height)) <= tolerance &&
                        r1.x < r2.x + r2.width && r2.x < r1.x + r1.width;
            case "south":
                return Math.abs((r1.y + r1.height) - r2.y) <= tolerance &&
                        r1.x < r2.x + r2.width && r2.x < r1.x + r1.width;
            case "east":
                return Math.abs((r1.x + r1.width) - r2.x) <= tolerance &&
                        r1.y < r2.y + r2.height && r2.y < r1.y + r1.height;
            case "west":
                return Math.abs(r1.x - (r2.x + r2.width)) <= tolerance &&
                        r1.y < r2.y + r2.height && r2.y < r1.y + r1.height;
            default:
                return false;
        }
    }


    public int getGridSize() {
        return gridSize;
    }

    public boolean isSnapToGridEnabled() {
        return snapToGrid;
    }

    public void setSnapToGrid(boolean snap) {
        this.snapToGrid = snap;
    }

    public FloorPlan() {
        setLayout(null);
        this.rooms = new ArrayList<>();
        this.setBackground(Color.LIGHT_GRAY);
        this.setLayout(null);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleWallOpeningClick(e);
            }
        });
    }

    private void handleWallOpeningClick(MouseEvent e) {
        Point clickPoint = e.getPoint();
        WallOpening selectedOpening = null;

        for (WallOpening opening : wallOpenings) {
            Rectangle openingBounds = getOpeningBounds(opening);
            if (openingBounds.contains(clickPoint)) {
                selectedOpening = opening;
                break;
            }
        }

        if (selectedOpening != null) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to delete this wall opening?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                wallOpenings.remove(selectedOpening);
                repaint();
            }
        }
    }


    private Rectangle getOpeningBounds(WallOpening opening) {
        Rectangle roomBounds = opening.getParentRoom().getBounds();
        Point loc = opening.getLocation();
        int width = opening.getOpeningWidth();

        switch (opening.getPosition()) {
            case "north" -> {
                return new Rectangle(loc.x, roomBounds.y - 5, width, 10); // clickable area on the north wall
            }
            case "south" -> {
                return new Rectangle(loc.x, roomBounds.y + roomBounds.height - 5, width, 10);
            }
            case "east" -> {
                return new Rectangle(roomBounds.x + roomBounds.width - 5, loc.y, 10, width);
            }
            case "west" -> {
                return new Rectangle(roomBounds.x - 5, loc.y, 10, width);
            }
            default -> {
                return new Rectangle(0, 0, 0, 0); // no valid bounds
            }
        }
    }




    public void toggleGridLines(boolean show) {
        showGridLines = show;
        repaint();
    }

    public void startDragging() {
        isDragging = true;
        repaint();
    }

    public void stopDragging() {
        isDragging = false;
        repaint();
    }


    public boolean overlapCheck(Room newRoom) {
        Rectangle newBounds = newRoom.getBounds();

        for (Room room : rooms) {
            if (room != newRoom) {
                Rectangle existingBounds = room.getBounds();

                // check for shared walls
                if (isSharedWall(newBounds, existingBounds)) {
                    continue;
                }

                if (newBounds.intersects(existingBounds)) {
                    return true; // overlap detected
                }
            }
        }

        return false; // No overlap found
    }

    private boolean isSharedWall(Rectangle bounds1, Rectangle bounds2) {
        return bounds1.x == bounds2.x + bounds2.width || // Right wall of bounds2 is left wall of bounds1
                bounds1.x + bounds1.width == bounds2.x || // Left wall of bounds2 is right wall of bounds1
                bounds1.y == bounds2.y + bounds2.height || // Bottom wall of bounds2 is top wall of bounds1
                bounds1.y + bounds1.height == bounds2.y;   // Top wall of bounds2 is bottom wall of bounds1
    }


    public void removeRoomWalls(Room room) {
        Graphics2D g2d = (Graphics2D) getGraphics();
        g2d.setColor(getBackground());
        g2d.setStroke(new BasicStroke(WALL_THICKNESS));

        Rectangle bounds = room.getBounds();
        g2d.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
        g2d.drawLine(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        g2d.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
        g2d.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
    }

    public void addRoom(Room room) {
        if (!overlapCheck(room)) {
            rooms.add(room);
            add(room);
            revalidate();
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Overlap detected, please fix to add room");
        }
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        remove(room);
        revalidate();
        repaint();
    }


    public void addItem(FloorPlan floorPlan, String itemType, String category) {
        boolean placed = false;
        for (Component comp : floorPlan.getComponents()) {
            if (comp instanceof Room) {
                Room room = (Room) comp;
                Rectangle roomBounds = room.getBounds();
                int x = roomBounds.x + (roomBounds.width / 2) - (40 / 2);
                int y = roomBounds.y + (roomBounds.height / 2) - (40 / 2);

                PlaceableItem item = new PlaceableItem(x, y, itemType, category);
                item.parentRoom = room;

                // Ensure no overlap
                boolean overlap = false;
                for (Component other : floorPlan.getComponents()) {
                    if (other instanceof PlaceableItem &&
                            other.getBounds().intersects(item.getBounds())) {
                        overlap = true;
                        break;
                    }
                }

                if (!overlap) {
                    floorPlan.add(item);
                    floorPlan.setComponentZOrder(item, 0); // Ensure items render on top
                    placed = true;
                    break;
                }
            }
        }

        // If no room was available, place in an open space on the grid
        if (!placed) {
            int gridSize = floorPlan.getGridSize();
            int startRow = 0, startCol = 0;

            while (!placed && startRow * gridSize < floorPlan.getHeight()) {
                int x = startCol * gridSize;
                int y = startRow * gridSize;

                PlaceableItem item = new PlaceableItem(x, y, itemType, category);
                boolean overlap = false;

                for (Component comp : floorPlan.getComponents()) {
                    if (comp instanceof PlaceableItem &&
                            comp.getBounds().intersects(item.getBounds())) {
                        overlap = true;
                        break;
                    }
                }

                if (!overlap) {
                    floorPlan.add(item);
                    floorPlan.setComponentZOrder(item, 0);
                    placed = true;
                }

                startCol++;
                if (x + gridSize > floorPlan.getWidth()) {
                    startCol = 0;
                    startRow++;
                }
            }
        }

        if (!placed) {
            JOptionPane.showMessageDialog(floorPlan, "No space available to place " + itemType,
                    "Placement Error", JOptionPane.WARNING_MESSAGE);
        }

        floorPlan.revalidate();
        floorPlan.repaint();
    }



    private boolean shouldPlaceInRoom(String itemType, String roomType) {
        return switch (itemType.toLowerCase()) {
            case "bed" -> roomType.equals("Bedroom");
            case "shower", "toilet", "washbasin" -> roomType.equals("Bathroom");
            case "kitchen sink", "stove" -> roomType.equals("Kitchen");
            case "sofa" -> roomType.equals("Living Room");
            case "dining set" -> roomType.equals("Dining Room");
            default -> false;
        };
    }


    public void clear() {
        rooms.clear();
        wallOpenings.clear();
        removeAll();
        revalidate();
        repaint();
    }


    public void toggleRoomLabels() {
        for (Room room : rooms) {
            room.setShowLabel(!room.isShowLabel());
        }
        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (showGridLines) {
            drawGrid(g);
        }
        if (!isDragging) {
            drawWalls(g);
        }
    }

    public void drawWalls(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(wallColor);
        g2d.setStroke(new BasicStroke(WALL_THICKNESS));

        // Draw basic walls first
        for (Room room : rooms) {
            Rectangle bounds = room.getBounds();
            drawWallsForRoom(g2d, bounds);
        }

        // Draw openings
        for (WallOpening opening : wallOpenings) {
            drawOpening(g2d, opening);
        }
    }


    private void drawOpening(Graphics2D g2d, WallOpening opening) {
        Rectangle roomBounds = opening.getParentRoom().getBounds();
        Point loc = opening.getLocation();
        int width = opening.getOpeningWidth();

        if (opening.getType() == WallOpening.Type.WINDOW) {
            // Draw window as dashed line
            g2d.setStroke(new BasicStroke(WALL_THICKNESS, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, WINDOW_DASH_PATTERN, 0.0f));
            g2d.setColor(Color.WHITE);
        }


        switch (opening.getPosition()) {
            case "north" -> {
                if (opening.getType() == WallOpening.Type.DOOR) {
                    g2d.setColor(getBackground());
                    g2d.drawLine(loc.x, roomBounds.y, loc.x + width, roomBounds.y);
                    g2d.setColor(wallColor);
                } else {
                    g2d.drawLine(loc.x, roomBounds.y, loc.x + width, roomBounds.y);
                }
            }
            case "south" -> {
                if (opening.getType() == WallOpening.Type.DOOR) {
                    g2d.setColor(getBackground());
                    g2d.drawLine(loc.x, roomBounds.y + roomBounds.height,
                            loc.x + width, roomBounds.y + roomBounds.height);
                    g2d.setColor(wallColor);
                } else {
                    g2d.drawLine(loc.x, roomBounds.y + roomBounds.height,
                            loc.x + width, roomBounds.y + roomBounds.height);
                }
            }
            case "east" -> {
                if (opening.getType() == WallOpening.Type.DOOR) {
                    g2d.setColor(getBackground());
                    g2d.drawLine(roomBounds.x + roomBounds.width, loc.y,
                            roomBounds.x + roomBounds.width, loc.y + width);
                    g2d.setColor(wallColor);
                } else {
                    g2d.drawLine(roomBounds.x + roomBounds.width, loc.y,
                            roomBounds.x + roomBounds.width, loc.y + width);
                }
            }
            case "west" -> {
                if (opening.getType() == WallOpening.Type.DOOR) {
                    g2d.setColor(getBackground());
                    g2d.drawLine(roomBounds.x, loc.y, roomBounds.x, loc.y + width);
                    g2d.setColor(wallColor);
                } else {
                    g2d.drawLine(roomBounds.x, loc.y, roomBounds.x, loc.y + width);
                }
            }
        }

        // Reset stroke for normal walls
        g2d.setStroke(new BasicStroke(WALL_THICKNESS));
    }


    private void drawWallsForRoom(Graphics2D g2d, Rectangle bounds) {//left,right,top,bottom walls
        g2d.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
        g2d.drawLine(bounds.x + bounds.width, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
        g2d.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
        g2d.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
    }


//    private boolean areRoomsAdjacent(Rectangle r1, Rectangle r2) {
//        // Check if rooms are touching vertically
//        boolean verticallyAdjacent =
//                (Math.abs(r1.x - r2.x) < WALL_THICKNESS ||
//                        Math.abs((r1.x + r1.width) - r2.x) < WALL_THICKNESS ||
//                        Math.abs(r1.x - (r2.x + r2.width)) < WALL_THICKNESS) &&
//                        (r1.y <= r2.y + r2.height && r2.y <= r1.y + r1.height);
//
//        // Check if rooms are touching horizontally
//        boolean horizontallyAdjacent =
//                (Math.abs(r1.y - r2.y) < WALL_THICKNESS ||
//                        Math.abs((r1.y + r1.height) - r2.y) < WALL_THICKNESS ||
//                        Math.abs(r1.y - (r2.y + r2.height)) < WALL_THICKNESS) &&
//                        (r1.x <= r2.x + r2.width && r2.x <= r1.x + r1.width);
//
//        return (verticallyAdjacent || horizontallyAdjacent);
//    }


    public void setWallProperties(int thickness, Color color) {
        WALL_THICKNESS = thickness;
        wallColor = color;
        repaint();
    }


    public void setGridSize() {
        String input = JOptionPane.showInputDialog(this, "Enter new grid size:", gridSize);
        if (input != null) {
            try {
                int newSize = Integer.parseInt(input);
                if (newSize > 0) {
                    setGridSize(newSize);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid integer.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setGridSize(int newSize) {
        this.gridSize = newSize;
        repaint();
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(200, 200, 200));

        // Draw horizontal lines first (rows)
        int rows = getHeight() / gridSize;
        for (int row = 0; row <= rows; row++) {
            int y = row * gridSize;
            g.drawLine(0, y, getWidth(), y);
        }

        // Then draw vertical lines (columns)
        int cols = getWidth() / gridSize;
        for (int col = 0; col <= cols; col++) {
            int x = col * gridSize;
            g.drawLine(x, 0, x, getHeight());
        }
    }


    private Point snapToGrid(Point p) {
        int row = Math.round((float) p.y / gridSize);
        int col = Math.round((float) p.x / gridSize);
        return new Point(col * gridSize, row * gridSize);
    }

    public void savePlan(File file) {
        try {
            if (!file.getName().endsWith(".plan")) {
                file = new File(file.getAbsolutePath() + ".plan");
            }

            Object[] data = {rooms, getPlaceableItems(),wallOpenings};

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(data);
            }

            JOptionPane.showMessageDialog(this, "Plan saved successfully as " + file.getName(),
                    "Save Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save plan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ArrayList<PlaceableItem> getPlaceableItems() {
        ArrayList<PlaceableItem> items = new ArrayList<>();
        for (Component comp : getComponents()) {
            if (comp instanceof PlaceableItem) {
                items.add((PlaceableItem) comp);
            }
        }
        return items;
    }


    public void loadPlan(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object[] data = (Object[]) ois.readObject();

            // Clear existing data
            rooms.clear();
            wallOpenings.clear();
            removeAll();

            // Restore rooms
            ArrayList<Room> loadedRooms = (ArrayList<Room>) data[0];
            for (Room room : loadedRooms) {
                room.setupMouseListeners(); // reinitialize transient fields
                rooms.add(room);
                add(room);
            }

            // Restore fixtures
            ArrayList<PlaceableItem> loadedItems = (ArrayList<PlaceableItem>) data[1];
            for (PlaceableItem item : loadedItems) {
                item.setupMouseListeners(); // Reinitialize transient fields
                add(item);
            }
            ArrayList<WallOpening> loadedWallOpenings = (ArrayList<WallOpening>) data[2];
            wallOpenings.addAll(loadedWallOpenings);
            revalidate();
            repaint();
            JOptionPane.showMessageDialog(this, "Plan loaded successfully!",
                    "Load Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load plan: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}




    class Room extends JPanel implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        protected final String roomType;

        private transient Point dragOffset;
        private transient boolean dragging;
        private transient boolean resizing;
        private static final int RESIZE_MARGIN = 10;
        private transient Point originalLocation;  // For overlap check
        private transient Dimension originalSize;
        boolean showRoomLabels = true;
        boolean isSelected = false;
        String[] roomTypes = {"Bedroom", "Bathroom", "Kitchen", "Living Room", "Dining Room"};


        public Room(int x, int y, int width, int height, String roomType) {
            this.setBounds(x, y, width, height);
            this.roomType = roomType;
            this.setBackground(getRoomColor(roomType));
            setupMouseListeners();
        }

        void setupMouseListeners() {
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showContextMenu(e);
                    } else if (isInResizeArea(e)) {
                        resizing = true;
                        originalSize = getSize();
                    } else {
                        dragging = true;
                        dragOffset = e.getPoint();
                        originalLocation = getLocation();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragging || resizing) {
                        FloorPlan parent = (FloorPlan) getParent();
                        // Check for overlap in final position
                        if (parent.overlapCheck(Room.this)) {
                            if (dragging) {                               // if overlap, revert to original position/size
                                setLocation(originalLocation);
                            } else {
                                setSize(originalSize);
                            }
                            JOptionPane.showMessageDialog(parent,
                                    "Cannot place room here - overlap detected",
                                    "Overlap Error",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            // Remove the walls and repaint the parent
                            parent.removeRoomWalls(Room.this);
                            parent.repaint();
                        }
                        parent.stopDragging();
                    }
                    dragging = false;
                    resizing = false;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    FloorPlan parent = (FloorPlan) getParent();

                    if (parent == null) return;

                    if (dragging) {
                        parent.startDragging();
                        Point newLocation = getLocation();
                        newLocation.translate(e.getX() - dragOffset.x, e.getY() - dragOffset.y);

                        if (parent.isSnapToGridEnabled()) {
                            int gridSize = parent.getGridSize();
                            int row = Math.round((float) newLocation.y / gridSize);
                            int col = Math.round((float) newLocation.x / gridSize);
                            newLocation.y = row * gridSize;
                            newLocation.x = col * gridSize;
                        }

                        // Ensure room stays within parent bounds
                        newLocation.x = Math.max(0, Math.min(newLocation.x, parent.getWidth() - getWidth()));
                        newLocation.y = Math.max(0, Math.min(newLocation.y, parent.getHeight() - getHeight()));

                        setLocation(newLocation);
                    } else if (resizing) {
                        // Calculate new size following row-major order
                        int newHeight = Math.max(50, e.getY());
                        int newWidth = Math.max(50, e.getX());

                        if (parent.isSnapToGridEnabled()) {
                            int gridSize = parent.getGridSize();
                            // Snap height (row) first, then width (column)
                            newHeight = Math.round((float) newHeight / gridSize) * gridSize;
                            newWidth = Math.round((float) newWidth / gridSize) * gridSize;
                        }

                        newWidth = Math.min(newWidth, parent.getWidth() - getX());
                        newHeight = Math.min(newHeight, parent.getHeight() - getY());

                        setSize(newWidth, newHeight);
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    setCursor(isInResizeArea(e) ?
                            Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR) :
                            Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

            };

            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();

            // Draw room label
            if (showRoomLabels) {
                int textWidth = fm.stringWidth(roomType);
                int textHeight = fm.getHeight();
                g.drawString(roomType, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2);

                // Draw dimensions
                String dimensions = getWidth() + " x " + getHeight();
                textWidth = fm.stringWidth(dimensions);
                g.drawString(dimensions, (getWidth() - textWidth) / 2, getHeight() - 5);

                // Draw resize handle
                g.setColor(Color.DARK_GRAY);
                g.fillRect(getWidth() - RESIZE_MARGIN, getHeight() - RESIZE_MARGIN, RESIZE_MARGIN, RESIZE_MARGIN);
            }
        }

        private void showContextMenu(MouseEvent e) {
            JPopupMenu contextMenu = new JPopupMenu();

            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(event -> {
                Container parent = getParent();
                if (parent instanceof FloorPlan) {
                    ((FloorPlan) parent).removeRoom(this);
                }
            });

            JMenuItem changeDimensionsItem = new JMenuItem("Change Dimensions");
            changeDimensionsItem.addActionListener(event -> changeDimensions());

            // adding  menu
            JMenu addRelative = new JMenu("Add Room Relatively");

            // submenu for north, south, east, and west positions
            JMenu relativeNorth = new JMenu("Add north");
            JMenu relativeSouth = new JMenu("Add south");
            JMenu relativeEast = new JMenu("Add east");
            JMenu relativeWest = new JMenu("Add west");

            // adding alignment options
            addAlignmentOptions(relativeNorth, "north");
            addAlignmentOptions(relativeSouth, "south");
            addAlignmentOptions(relativeEast, "east");
            addAlignmentOptions(relativeWest, "west");


            addRelative.add(relativeNorth);
            addRelative.add(relativeSouth);
            addRelative.add(relativeEast);
            addRelative.add(relativeWest);

            // adding items to context menu
            contextMenu.add(deleteItem);
            contextMenu.add(changeDimensionsItem);
            contextMenu.add(addRelative);


            contextMenu.show(this, e.getX(), e.getY());
        }

        private void addAlignmentOptions(JMenu directionMenu, String direction) {
            JMenuItem leftAlign = new JMenuItem("Left");
            JMenuItem centerAlign = new JMenuItem("Center");
            JMenuItem rightAlign = new JMenuItem("Right");


            leftAlign.addActionListener(event -> promptAddRoom(direction, "left"));
            centerAlign.addActionListener(event -> promptAddRoom(direction, "center"));
            rightAlign.addActionListener(event -> promptAddRoom(direction, "right"));

            directionMenu.add(leftAlign);
            directionMenu.add(centerAlign);
            directionMenu.add(rightAlign);
        }

        private void addRoomRelative(String direction, String alignment, String roomType) {
            Container parent = getParent();
            if (parent instanceof FloorPlan floorPlan) {
                Room newRoom = calculateNewRoomPosition(direction, alignment, roomType);

                if (!floorPlan.overlapCheck(newRoom)) {
                    floorPlan.addRoom(newRoom);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Room placement would cause overlap. Try a different position.",
                            "Placement Error",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        private void promptAddRoom(String direction, String alignment) {
            String roomType = (String) JOptionPane.showInputDialog(
                    this,
                    "Select or enter the room type for " + alignment + " alignment",
                    "Room Type",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    roomTypes,
                    roomTypes[0]
            );

            if (roomType != null && !roomType.isEmpty()) {
                addRoomRelative(direction, alignment, roomType);
            }
        }


        private Room calculateNewRoomPosition(String direction, String alignment, String roomType) {
            System.out.println("Direction: " + direction + ", Alignment: " + alignment);

            //default
            int newRoomWidth = 150;
            int newRoomHeight = 100;

            int currentX = getX();
            int currentY = getY();
            int currentWidth = getWidth();
            int currentHeight = getHeight();

            System.out.println("Current values:");
            System.out.println("X: " + currentX + ", Y: " + currentY);
            System.out.println("Width: " + currentWidth + ", Height: " + currentHeight);
            System.out.println("New room dimensions: " + newRoomWidth + "x" + newRoomHeight);

            int newX = currentX;
            int newY = currentY;
            int currentCenterX = currentX + (currentWidth / 2);
            int currentCenterY = currentY + (currentHeight / 2);

            switch (direction.toLowerCase()) {
                case "north" -> {
                    newY = currentY - newRoomHeight;
                    switch (alignment.toLowerCase()) {
                        case "left" -> newX = currentCenterX- newRoomWidth;
                        case "center" -> newX = currentCenterX - (newRoomWidth / 2);
                        case "right" -> newX = currentCenterX ;
                    }
                }
                case "south" -> {
                    newY = currentY + currentHeight;  // Place below current room
                    switch (alignment.toLowerCase()) {
                        case "left" -> newX = currentCenterX;  // Align current center with new room's left
                        case "center" -> newX = currentCenterX - (newRoomWidth / 2);  // Center align
                        case "right" ->
                                newX = currentCenterX - newRoomWidth;  // Align current center with new room's right
                    }
                }
                case "east" -> {
                    newX = currentX + currentWidth;  // Place to the right
                    switch (alignment.toLowerCase()) {
                        case "left" -> newY = currentCenterY;  // Align current center with new room's top
                        case "center" -> newY = currentCenterY - (newRoomHeight / 2);  // Center align
                        case "right" ->
                                newY = currentCenterY - newRoomHeight;  // Align current center with new room's bottom
                    }
                }
                case "west" -> {
                    newX = currentX - newRoomWidth;  // Place to the left
                    switch (alignment.toLowerCase()) {
                        case "left" -> newY = currentCenterY;  // Align current center with new room's top
                        case "center" -> newY = currentCenterY - (newRoomHeight / 2);  // Center align
                        case "right" ->
                                newY = currentCenterY - newRoomHeight;  // Align current center with new room's bottom
                    }
                }
            }


            System.out.println("New position: X=" + newX + ", Y=" + newY);

            return new Room(newX, newY, newRoomWidth, newRoomHeight, roomType);
        }

        private void changeDimensions() {
            JTextField widthField = new JTextField(String.valueOf(getWidth()));
            JTextField heightField = new JTextField(String.valueOf(getHeight()));

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Width:"));
            panel.add(widthField);
            panel.add(new JLabel("Height:"));
            panel.add(heightField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Enter new dimensions", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    int width = Math.max(50, Integer.parseInt(widthField.getText()));
                    int height = Math.max(50, Integer.parseInt(heightField.getText()));

                    Container parent = getParent();
                    if (parent != null) {
                        width = Math.min(width, parent.getWidth() - getX());
                        height = Math.min(height, parent.getHeight() - getY());
                    }

                    setSize(width, height);
                    revalidate();
                    repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid input. Please enter valid integers.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


        private boolean isInResizeArea(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            return (
                    (x >= getWidth() - RESIZE_MARGIN && y >= getHeight() - RESIZE_MARGIN) || // Lower right corner
                            (x >= getWidth() - RESIZE_MARGIN && y <= RESIZE_MARGIN) // Upper right corner
            );

        }

        public static Color getRoomColor(String roomType) {
            return switch (roomType) {
                case "Bedroom" -> new Color(144, 238, 144);
                case "Bathroom" -> new Color(100, 100, 240);
                case "Kitchen" -> new Color(255, 100, 100);
                case "Living Room" -> new Color(255, 255, 100);
                case "Dining Room" -> new Color(255, 218, 185);
                default -> Color.LIGHT_GRAY;
            };

        }

        public boolean isShowLabel() {
            return showRoomLabels;
        }

        public void setShowLabel(boolean b) {
            this.showRoomLabels = b;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }


    }



class PlaceableItem extends JPanel implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    protected String itemType;
    protected String category; // fixture or furniture
    protected int rotation = 0;
    private  Point dragOffset;
    private  boolean dragging = false;
    private  Point originalLocation;
    protected transient ImageIcon icon;
    private static final int DEFAULT_WIDTH = 30;
    private static final int DEFAULT_HEIGHT = 30;
    protected Room parentRoom = null; // track which room the item is in


    public PlaceableItem(int x, int y, String itemType, String category) {
        this.itemType = itemType;
        this.category = category;
        this.setBounds(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        icon = IconHelper.getIcon(itemType, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setupMouseListeners();
        setOpaque(false); // Make background transparent
    }
    private void setupIcon(String imagePath) {
        try {
            icon = new ImageIcon(imagePath);
            Image scaled = icon.getImage().getScaledInstance(DEFAULT_WIDTH, DEFAULT_HEIGHT, Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        } catch (Exception e) {
            // alt
            setBackground(category.equals("fixture") ? new Color(200, 200, 255) : new Color(255, 200, 200));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (icon != null) {
            AffineTransform old = g2d.getTransform();

            g2d.rotate(Math.toRadians(rotation), getWidth() / 2.0, getHeight() / 2.0);
            g2d.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);

            g2d.setTransform(old);
        }

        // draw item label
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String label = itemType;
        int textWidth = fm.stringWidth(label);
        g2d.drawString(label, (getWidth() - textWidth) / 2, getHeight() + 15);
    }

    void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                } else {
                    dragging = true;
                    dragOffset = e.getPoint();
                    originalLocation = getLocation();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging) {
                    handleItemDrop();
                }
                dragging = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    Point newLocation = getLocation();
                    newLocation.translate(
                            e.getX() - dragOffset.x,
                            e.getY() - dragOffset.y
                    );

                    // Keep within parent bounds
                    Container parent = getParent();
                    if (parent != null) {
                        newLocation.x = Math.max(0, Math.min(newLocation.x, parent.getWidth() - getWidth()));
                        newLocation.y = Math.max(0, Math.min(newLocation.y, parent.getHeight() - getHeight()));
                    }

                    setLocation(newLocation);
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private void handleItemDrop() {
        FloorPlan floorPlan = (FloorPlan) getParent();
        if (floorPlan == null) return;

        Point itemCenter = new Point(
                getX() + getWidth() / 2,
                getY() + getHeight() / 2
        );

        // check for overlap with other items
        boolean overlapWithItems = false;
        Room newParentRoom = null;

        for (Component comp : floorPlan.getComponents()) {
            if (comp != this) {
                if (comp instanceof PlaceableItem && comp.getBounds().intersects(getBounds())) {
                    overlapWithItems = true;
                    break;
                } else if (comp instanceof Room && comp.getBounds().contains(itemCenter)) {
                    newParentRoom = (Room) comp;
                }
            }
        }

        if (overlapWithItems) {
            // revert to original position if overlapping with other items
            setLocation(originalLocation);
            JOptionPane.showMessageDialog(floorPlan,
                    "Cannot place item here - overlaps with another item",
                    "Overlap Error",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            // Update parent room
            parentRoom = newParentRoom;

            // Snap to grid if enabled
            if (floorPlan.isSnapToGridEnabled()) {
                snapToGrid();
            }
        }
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem rotateItem = new JMenuItem("Rotate 90°");
        rotateItem.addActionListener(event -> {
            rotation = (rotation + 90) % 360;
            repaint();
        });

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(event -> {
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
            }
        });

        JMenu sizeMenu = new JMenu("Adjust Size");

        JMenuItem increaseSizeItem = new JMenuItem("Increase Size");
        increaseSizeItem.addActionListener(e2 -> {
            setSize(getWidth() + 10, getHeight() + 10);
            revalidate();
            repaint();
        });

        JMenuItem decreaseSizeItem = new JMenuItem("Decrease Size");
        decreaseSizeItem.addActionListener(e2 -> {
            if (getWidth() > 20 && getHeight() > 20) {
                setSize(getWidth() - 10, getHeight() - 10);
                revalidate();
                repaint();
            }
        });

        sizeMenu.add(increaseSizeItem);
        sizeMenu.add(decreaseSizeItem);

        contextMenu.add(rotateItem);
        contextMenu.add(sizeMenu);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);
        contextMenu.show(this, e.getX(), e.getY());
    }




    private void snapToGrid() {
        Container parent = getParent();
        if (parent instanceof FloorPlan) {
            FloorPlan floorPlan = (FloorPlan) parent;
            if (floorPlan.isSnapToGridEnabled()) {
                int gridSize = floorPlan.getGridSize();
                Point loc = getLocation();
                setLocation(
                        Math.round((float)loc.x / gridSize) * gridSize,
                        Math.round((float)loc.y / gridSize) * gridSize
                );
            }
        }
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject(); // Serialize default fields
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Perform default deserialization

        icon = IconHelper.getIcon(itemType, DEFAULT_WIDTH, DEFAULT_HEIGHT);

        setupMouseListeners();
    }




}


class WallOpening implements Serializable {
    enum Type { DOOR, WINDOW }
    private Type type;
    private Point location;
    private String position; // north, south, east, west
    private Room parentRoom;
    private Room connectedRoom; // null if opening to outside
    private static final int OPENING_WIDTH = 40;
    private static final float[] WINDOW_DASH_PATTERN = {5.0f, 5.0f};

    public WallOpening(Type type, Point location, String position, Room parentRoom, Room connectedRoom) {
        this.type = type;
        this.location = location;
        this.position = position;
        this.parentRoom = parentRoom;
        this.connectedRoom = connectedRoom;
    }

    // getting wall and room attributes
    public Type getType() { return type; }
    public Point getLocation() { return location; }
    public String getPosition() { return position; }
    public Room getParentRoom() { return parentRoom; }
    public Room getConnectedRoom() { return connectedRoom; }
    public int getOpeningWidth() { return OPENING_WIDTH; }
}
class IconHelper {
private static final Map<String, String> iconPaths = new HashMap<>(); // mapping item types to their image paths
private static final Map<String, ImageIcon> originalIconCache = new HashMap<>(); // cache for original icons (unscaled)

static {
    // adding paths for fixtures
    iconPaths.put("Toilet", "C:/Users/Shashwat Singh/Desktop/fixtures/toilet.png");
    iconPaths.put("Shower", "C:/Users/Shashwat Singh/Desktop/fixtures/shower.png");
    iconPaths.put("Kitchen Sink", "C:/Users/Shashwat Singh/Desktop/fixtures/kitchen.png");
    iconPaths.put("WashBasin", "C:/Users/Shashwat Singh/Desktop/fixtures/basin.png");
    iconPaths.put("Stove", "C:/Users/Shashwat Singh/Desktop/fixtures/gas-stove.png");

    // adding paths for furniture
    iconPaths.put("Bed", "C:/Users/Shashwat Singh/Desktop/fixtures/bed.png");
    iconPaths.put("Table", "C:/Users/Shashwat Singh/Desktop/fixtures/table.png");
    iconPaths.put("Chair", "C:/Users/Shashwat Singh/Desktop/fixtures/chair.png");
    iconPaths.put("Sofa", "C:/Users/Shashwat Singh/Desktop/fixtures/sofa.png");
    iconPaths.put("Dining Set", "C:/Users/Shashwat Singh/Desktop/fixtures/dining-table.png");
}

public static ImageIcon getIcon(String itemType, int width, int height) {
    // cache the original icon if not already cached
    if (!originalIconCache.containsKey(itemType)) {
        String path = iconPaths.get(itemType); // get the path for the item type
        if (path != null) {
            originalIconCache.put(itemType, new ImageIcon(path)); // cache the original icon without scaling
        }
    }

    // scale the original image dynamically to the requested dimensions
    ImageIcon originalIcon = originalIconCache.get(itemType);
    if (originalIcon != null) {
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage); // return the scaled icon
    }

    return null; // return null if no icon is available for the item type
}
}


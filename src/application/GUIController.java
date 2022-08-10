package application;

import java.io.FileInputStream;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GUIController {
	Stage applicationStage;
	Scene mainScene;
	TriangleCatalog triangleStates = new TriangleCatalog();
	boolean mainSceneSet = false;
	int highlightedPanelIndex = 0;
    Triangle triangle;
    Triangle oldTriangle;
	
    @FXML
    private TextField hypotenuseTextField, adjacentTextField, oppositeTextField, angleThetaTextField;
    @FXML
    private ToggleButton degreesToggleButton, radiansToggleButton, valueToggleButton, formulaToggleButton;
	@FXML
	private Button informationButton, nextButton, previousButton;
    @FXML
    private Canvas mainCanvas = new Canvas();
    @FXML
    private Text infoAreaText;
    @FXML
    private Label errorLabel, instructionLabel; 
	@FXML
	private VBox canvasVBox;
    /**
     * Navigates through the necessary processes to display the result to the user. This
     * includes passing the user inputs to the triangle class, setting error labels, and
     * calling the drawing and labelling functions as needed.
     */
    @FXML
    void calculate() {
    	errorLabel.setText("");
    	
    	if(checkTwoTotalInputs()) {
    		if(triangle != null) oldTriangle = new Triangle(triangle);
    		else oldTriangle = triangle;

        	//creating a new triangle or FormulaTriangle object, giving it the contents of the GUI.
        	//Triangle calculates based off numeric values, whereas FormulaTriangle is a child 
    		//of Triangle which overrides certain methods in order to handle string entries
    		//to calculate for an algebraic formula which incorporates the user's inputs instead.
        	boolean angleModeDegrees = degreesToggleButton.isSelected() ? true : false;
        	if(formulaToggleButton.isSelected()) {
        		triangle = new FormulaTriangle(hypotenuseTextField.getText(),oppositeTextField.getText(),
        										adjacentTextField.getText(),angleThetaTextField.getText(), 
        										angleModeDegrees, mainCanvas);
        	} else {
        		triangle = new Triangle(hypotenuseTextField.getText(),oppositeTextField.getText(),
        								adjacentTextField.getText(),angleThetaTextField.getText(), 
        								angleModeDegrees, mainCanvas);
        	}
        	//setting the errorLabel to notify user of any potential errors regarding inputs
        	errorLabel.setText(triangle.getErrorDescription());
        	
    		//checking that the triangle no longer has any sidelengths/angle at a value of 0
        	if(oldTriangle == null || oldTriangle.isDifferent(triangle)) {
        		if(triangle.isValidTriangle()) {
        			//calls the methods to draw the triangle, it's labels, and the information associated with the triangle
        			drawAllTriangleComponents(mainCanvas);
        			triangleStates.addTriangle(oldTriangle, triangle);
        			addCanvasPanel();
        		} else {
            		if(errorLabel.getText().isEmpty()) 
                		//if the triangle has 0 for h/o/a values, no errorlabel message change is needed as the
            			//validateInput() method would already have set an errorlabel message, and should not be 
            			//overwritten. Therefore there is only an errorlabel message for the isNaN case of validateTriangle().
            			errorLabel.setText("Opp. and Adj. can't be larger than or equal to Hyp.");
        		}
        	}
    	}
    }
   
    
    public void drawAllTriangleComponents(Canvas canvasToDrawOn) {
    	triangle.prepareForCanvas(canvasToDrawOn);
    	
		//drawing the outline of the triangle
    	drawTriangle(canvasToDrawOn);
    	
    	//moving and setting labels according to the values of the triangle
    	setTriangleLabels(canvasToDrawOn, true);
    	
    	//putting the solve method in the text area of the GUI.
    	setInfoText();
    }
    
    
	/**
	 * Draws the triangle by stroking between triangle points.
	 * @param graphics - GraphicsContext object to draw with
	 */
	public void drawTriangle(Canvas canvasToDrawOn) {
		GraphicsContext graphics = canvasToDrawOn.getGraphicsContext2D();
		
		//clears the canvas so that the previously drawn triangle (if applicable) is cleared.
		graphics.setFill(Color.BLACK);
		graphics.clearRect(0, 0, canvasToDrawOn.getWidth(), canvasToDrawOn.getHeight());
		graphics.fillRect(0, 0, canvasToDrawOn.getWidth(), canvasToDrawOn.getHeight());
		graphics.setFill(Color.WHITE);
		graphics.fillRect(1, 1, canvasToDrawOn.getWidth()-2, canvasToDrawOn.getHeight()-2);
		graphics.setFill(Color.BLACK);
		
		//setting triangle values to formulas to clean up further calculations
		double haX = triangle.getHa().getX();
		double haY = triangle.getHa().getY();
		double hoX = triangle.getHo().getX();
		double hoY = triangle.getHo().getY();
		double oaX = triangle.getOa().getX();
		double oaY = triangle.getOa().getY();
		
		//strokes between each point of the triangle
		graphics.strokeLine(haX, haY, hoX, hoY);
		graphics.strokeLine(hoX, hoY, oaX, oaY);
		graphics.strokeLine(oaX, oaY, haX, haY);
	}
	
	/**
	 * Moves and sets the labels for the triangle to their 
	 * respective side-length/angle positions and values.
	 * @param graphics - GraphicsContext object to draw with
	 */
	public void setTriangleLabels(Canvas canvasToDrawOn, boolean calculatePositions) {
		GraphicsContext graphics = canvasToDrawOn.getGraphicsContext2D();
		
		if(calculatePositions) {
			//setting minimum bounds for labels (so they are fully visible)
			int xBound = 335;
			int yBound = 10;
			
			//setting values for overlap detection between labels
			int overlapMinX = 100;
			int overlapMinY = 15;
			
			//creating a new point for each label to be drawn at
			Point h = calculateMidpoint(triangle.getHa(), triangle.getHo(), xBound, yBound);
			Point o = calculateMidpoint(triangle.getHo(), triangle.getOa(), xBound, yBound);
			Point a = calculateMidpoint(triangle.getHa(), triangle.getOa(), xBound, yBound);
			Point t = calculateMidpoint(triangle.getHa(), triangle.getHa(), xBound, yBound);
			
			//move the newly created points such that they do not overlap to keep labels readable
			moveOverlappingPoints(h, o, a, t, overlapMinX, overlapMinY);
			
			//writes the label information at their respective locations
			graphics.setFill(Color.RED);
			graphics.fillText("H: " + triangle.getInfo("h"), h.getX(), h.getY());
			graphics.fillText("O: " + triangle.getInfo("o"), o.getX(), o.getY());
			graphics.fillText("A: " + triangle.getInfo("a"), a.getX(), a.getY());
			graphics.fillText("θ: " + triangle.getInfo("t"), t.getX(), t.getY());
		} else {
			graphics.setFill(Color.RED);
			graphics.fillText("H: " + triangle.getInfo("h"), 0, 10);
			graphics.fillText("O: " + triangle.getInfo("o"), 0, 20);
			graphics.fillText("A: " + triangle.getInfo("a"), 0, 30);
			graphics.fillText("θ: " + triangle.getInfo("t"), 0, 40);
		}
	}
	
	/**
	 * puts the solve method information for the triangle in the infoText area of the GUI.
	 */
	public void setInfoText() {
    	infoAreaText.setText("Hypotenuse: " + triangle.getInfo("h") + 
    			"\nOpposite: " + triangle.getInfo("o") + 
    			"\nAdjacent: " + triangle.getInfo("a") + 
    			"\nAngle θ: " + triangle.getInfo("t")  + 
    			"\n\nTrig. Formula Used: " + triangle.getInfo("solveMethod"));
	}
    
    /**
     * Toggles the degrees and radians button in the GUI such that one and only one is always active.
     * @param trigger - radians or degrees GUI button that is clicked by the user
     */
    @FXML
    void toggleAngleMode(ActionEvent trigger) {
    	//determines which button was clicked, and sets it to true, 
    	//while also setting the other button false.
    	if(trigger.getSource().equals(degreesToggleButton)) {
    		radiansToggleButton.setSelected(false);
    		degreesToggleButton.setSelected(true);
    	} else {
    		degreesToggleButton.setSelected(false);
    		radiansToggleButton.setSelected(true);
    	}
    }
    
    /**
     * Toggles the value and formula button in the GUI such that one and only one is always active.
     * Sets the label at the top of GUI to prompt user to input the correct value type.
     * @param trigger - value or formula GUI button that is clicked by the user
     */
    @FXML
    void toggleValueMode(ActionEvent trigger) {
    	//determines which button was clicked, and sets it to true, 
    	//while also setting the other button false.
    	if(trigger.getSource().equals(valueToggleButton)) {
    		formulaToggleButton.setSelected(false);
    		valueToggleButton.setSelected(true);
    		instructionLabel.setText("Enter Two Numeric Values: ");
    	} else {
    		valueToggleButton.setSelected(false);
    		formulaToggleButton.setSelected(true);
    		instructionLabel.setText("Enter Two Strings (see Information): ");
    	}
    }
    
    /**
     * Checks if exactly two values were input by the user within the text fields.
     * @return true if exactly two values were entered, otherwise false
     */
    boolean checkTwoTotalInputs() {
    	int totalInputs = 0;
    	
    	//checks each text field and increments the counter for each text field that isn't empty
    	if (hypotenuseTextField.getText().isEmpty()) totalInputs++;
    	if (oppositeTextField.getText().isEmpty()) totalInputs++;
    	if (adjacentTextField.getText().isEmpty()) totalInputs++;
    	if (angleThetaTextField.getText().isEmpty()) totalInputs++;
    	if (totalInputs != 2) {
    		errorLabel.setText("Only enter values for two components.");
    		return false;
    	}
    	return true;
    }
    
	/**
	 * Checks for points that are too close to each other and moves them to prevent overlapping text. 
	 * @param h - hypotenuse label point
	 * @param o - opposite label point
	 * @param a - adjacent label point
	 * @param t - theta - angle label point
	 **/
	void moveOverlappingPoints(Point h, Point o, Point a, Point t, int minX, int minY) {
		//creation of array to loop through
		Point[] p = {h,o,a,t};
		
		//2d for loop such that all points are compared to each other
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				// no need to compare the same point to itself
				if(j==i) continue;
				
				//retrieving x and y coordinates of each point to reduce code clutter in next action
				double p1X = p[i].getX();
				double p1Y = p[i].getY();
				double p2X = p[j].getX();
				double p2Y = p[j].getY();
				
				//move point to minX/Y relative to other point if they are too close
				if(Math.abs(p1Y - p2Y) < minY && Math.abs(p1X - p2X) < minX) {
					if(p1Y <= p2Y) p[j].setY(p2Y + minY - (p2Y-p1Y));
					else p[i].setY(p1Y + minY - (p1Y-p2Y));
				}
			}
		}
	}
	 
	/**
	 * Uses the average x and y coordinate from p1 and p2 to create a new point.
	 * Sets coordinates to xBound/yBound if they do not fall within those values (causing them to not appear in canvas)
	 * @param p1 - first point to take coordinates from
	 * @param p2 - second point to take coordinates from
	 * @param xBound - maximum x coordinate of new point
	 * @param yBound - minimum y coordinate of new point
	 * @return new point object with coordinates averaged from the two points
	 */
	Point calculateMidpoint(Point p1, Point p2, int xBound, int yBound) {
		//get the average X coordinate between the two points
		double newX = (p1.getX()+p2.getX())/2;
		
		//get the average X coordinate between the two points
		double newY = (p1.getY()+p2.getY())/2;
		
		//set coordinate to max/min bound if outside of bound
		double x = (newX > xBound) ? xBound : newX;
		double y = (newY < yBound) ? yBound : newY;
		
		Point point = new Point(x,y);
		return point;
	}
	
	@FXML
	void switchToAboutScene(){
		if(!mainSceneSet) {
			mainScene = applicationStage.getScene();
			mainSceneSet = true;
		}
		
		Button backButton = new Button("Back to Calculator");
		backButton.setOnAction(doneEvent -> applicationStage.setScene(mainScene));
		
		Button validationInfoButton = new Button("Validation Info.");
		validationInfoButton.setOnAction(doneEvent -> switchToValidationScene());
		
		VBox buttonBox = new VBox();
		buttonBox.setAlignment(Pos.TOP_RIGHT);
		buttonBox.getChildren().addAll(validationInfoButton, backButton);
		
		Label headerLabel = new Label("About");
		headerLabel.setFont(new Font("Verdana", 30));
		
		Label infoLabel = new Label("This program creates a right triangle based off two entered values "
				+ "(sidelengths or angle) and outputs the values of all sidelengths and the inner angle. "
				+ "\n\nDegree / Radians: \nChanges whether the value entered, as well as the final value "
				+ "is read and shown as Degrees or Radians. \n\nValue: \nSolves for numeric values. negative "
				+ "Values may be entered for the opposite and adjacent side lengths. "
				+ "\n\nFormula: \nSolves for transformative algebraic formulas. Useful for figuring out what "
				+ "formulas to use in your programming projects. For example, entering distance(a,b) and "
				+ "a.getX()-b.getX() for the hypotenuse and adjacent respectively to find an algebraic formula "
				+ "for getting the angle between the two objects.");
		infoLabel.setWrapText(true);
		infoLabel.setFont(new Font(15));
		
		VBox labelBox = new VBox();
		labelBox.setMaxWidth(245);
		labelBox.getChildren().addAll(headerLabel, infoLabel);
		
		HBox.setMargin(labelBox, new Insets(10,10,10,10));
		HBox.setMargin(buttonBox, new Insets(10,10,10,10));
		
		HBox mainBox = new HBox();
		mainBox.getChildren().addAll(labelBox,buttonBox);
		
		applicationStage.setScene(new Scene(mainBox, mainScene.getWidth(), mainScene.getHeight()));
		
		validationInfoButton.setPrefWidth(backButton.getBoundsInParent().getWidth());
	}
	
	@FXML
	void switchToValidationScene() {
		Button infoButton = new Button("Back to Info.");
		infoButton.setOnAction(doneEvent -> switchToAboutScene());
		
		Button backButton = new Button("Back to Calculator");
		backButton.setOnAction(doneEvent -> applicationStage.setScene(mainScene));
		
		VBox buttonBox = new VBox();
		buttonBox.setAlignment(Pos.TOP_RIGHT);
		buttonBox.getChildren().addAll(infoButton, backButton);
		
		Label headerLabel = new Label("Validation");
		headerLabel.setFont(new Font("Verdana", 30));
		
		Label infoLabel = new Label("Entered values which do not meet these requirements will be "
				+ "accompanied with an error message. \n\nValue Mode:\nAll values may include a decimal "
				+ "for increased precision. \nHypotenuse: Positive values only. \nOpposite: Positive or "
				+ "negative values. \nAdjacent: Positive or negative values. \nAngle θ: Positive values "
				+ "only. Must be under 90° in Degrees mode, or π/2 in Radians mode.\n\nFormula Mode: "
				+ "\nAll entries are valid." );
		infoLabel.setWrapText(true);
		infoLabel.setFont(new Font(17));
		
		VBox labelBox = new VBox();
		labelBox.setMaxWidth(245);
		labelBox.getChildren().addAll(headerLabel, infoLabel);
		
		HBox.setMargin(labelBox, new Insets(10,10,10,10));
		HBox.setMargin(buttonBox, new Insets(10,10,10,10));
		
		HBox mainBox = new HBox();
		mainBox.getChildren().addAll(labelBox,buttonBox);
		
		applicationStage.setScene(new Scene(mainBox, mainScene.getWidth(), mainScene.getHeight()));
		
		infoButton.setPrefWidth(backButton.getBoundsInParent().getWidth());
	}
	
	@FXML
	void drawAdjacentTriangle(ActionEvent trigger) {
		oldTriangle = new Triangle(triangle);
		if(trigger.getSource() == nextButton) {
			triangle = triangleStates.getNextTriangle(triangle);
			if(oldTriangle.isDifferent(triangle)) updateHighlightedPanel(highlightedPanelIndex+1);	
		} else {
			triangle = triangleStates.getPreviousTriangle(triangle);
			if(oldTriangle.isDifferent(triangle)) updateHighlightedPanel(highlightedPanelIndex-1);	
		}
	}
	
	void selectAndDrawPanel(Canvas canvasClicked) {
		updateHighlightedPanel(canvasClicked);
		triangle = triangleStates.getTriangle(highlightedPanelIndex);
		drawAllTriangleComponents(mainCanvas);
	}
	
	@FXML
	void addCanvasPanel() {
		Canvas canvas = new Canvas(80,80);
		canvasVBox.getChildren().add(canvas);
		triangle.prepareForCanvas(canvas);
		canvas.setOnMousePressed(touchEvent -> selectAndDrawPanel(canvas));
		
		drawTriangle(canvas);
		setTriangleLabels(canvas, false);
		updateHighlightedPanel(canvas);
	}
	
	void updateHighlightedPanel(Canvas canvasToHighlight) {
		for(int i = 0; i < canvasVBox.getChildren().size(); i++) {
			if(canvasVBox.getChildren().get(i) != canvasToHighlight) {
				canvasVBox.getChildren().get(i).setOpacity(0.5);
			} else {
				canvasVBox.getChildren().get(i).setOpacity(1);
				highlightedPanelIndex = i;
			}
		}
	}
	
	void updateHighlightedPanel(int canvasIndexToHighlight) {
		for(int i = 0; i < canvasVBox.getChildren().size(); i++) {
			if(canvasVBox.getChildren().get(i) != canvasVBox.getChildren().get(canvasIndexToHighlight)) {
				canvasVBox.getChildren().get(i).setOpacity(0.5);
			} else {
				canvasVBox.getChildren().get(i).setOpacity(1);
				highlightedPanelIndex = i;
			}
		}
	}
}



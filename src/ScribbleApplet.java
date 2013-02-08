
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ScribbleApplet extends Applet {
	// paint count flag
	int paintCountFlag;
	//
	Image img;
	// Panels
	Panel centerMainPanel, southMainPanel;

	// sub-panels for southMainPanel
	Panel sliderPanel, buttonPanel;


	// preview panel
	Panel previewPanel = new Panel();

	// r,g,b sliders
	Scrollbar rSlider, gSlider, bSlider;

	// Labels for r,g,b sliders
	Label rLabel, gLabel, bLabel;

	// current r,g,b values
	int rValue = 0, gValue = 0 , bValue = 0;

	// brush
	Color brushColor = Color.BLACK;
	int brushStroke = 16;

	// buttons
	Button clearCanvasButton, resetBrushButton;

	// drawing canvas
	DrawingCanvas canvas = new DrawingCanvas();
	PreviewCanvas previewCanvas = new PreviewCanvas();
	// Constants
	final String COLOUR_CHOOSER_STR = "Colour Chooser";
	final Font MONOSPACED = new Font("Courier",Font.BOLD,14);
	final int DEFAULT_BRUSH_STROKE = 16;

	// Mouse positions
	int currentX, currentY, oldX, oldY;

	// 
	Point lineStart = new Point(0,0);


	public void init() {
		setSize(500,600);
		img = createImage(500,500);

		setGUI();
		addListeners();
	}

	protected void addListeners(){

		// buttons
		clearCanvasButton.addActionListener(new ButtonHandler());	  
		resetBrushButton.addActionListener(new ButtonHandler());

		// sliders
		rSlider.addAdjustmentListener(new SliderHandler());
		gSlider.addAdjustmentListener(new SliderHandler());
		bSlider.addAdjustmentListener(new SliderHandler());

		previewCanvas.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				Graphics offscreen = img.getGraphics();
				Graphics2D offscreen2d = (Graphics2D) offscreen;
				offscreen2d.setColor(brushColor);
				offscreen2d.fillRect(0,0,500,500);

				canvas.getGraphics().drawImage(img,0,0, canvas);
			}
		});
		// stroke adjust
		canvas.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e){
				if(e.getKeyChar() == '+')
					brushStroke++;
				if(e.getKeyChar() == '-')
					brushStroke--;


				Graphics gPreview = previewCanvas.getGraphics();
				Graphics g2dPreview = (Graphics2D) gPreview;
				g2dPreview.setColor(Color.WHITE);
				g2dPreview.fillRect(0,0,50,50);
				g2dPreview.setColor(brushColor);
				g2dPreview.fillRect(0,0,brushStroke,brushStroke);
			}
		});

		// canvas
		canvas.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
				currentX = e.getX();
				currentY = e.getY();
				oldX = currentX;
				oldY = currentY;


				Graphics offscreen = img.getGraphics();

				Graphics2D offscreen2d = (Graphics2D) offscreen;

				offscreen.setColor(brushColor);
				if (e.isShiftDown())  {	
					offscreen.fillOval(currentX-brushStroke/2, currentY-brushStroke/2, brushStroke+5, brushStroke+5);
				}
				else{
					offscreen2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
					offscreen2d.setStroke(new BasicStroke(brushStroke));
					offscreen.drawLine(lineStart.x, lineStart.y, currentX, currentY);	
				}

				canvas.getGraphics().drawImage(img,0,0, canvas);
				lineStart.move(currentX,currentY);
			}

			public void mouseMoved(MouseEvent e){

			}
		});

		canvas.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				int x = e.getX(), y = e.getY();
				lineStart.move(x,y);
			}
		});
	}



	class SliderHandler implements AdjustmentListener{
		public void adjustmentValueChanged(AdjustmentEvent e){

			// Display colour value
			String valueString = "";
			int value = ((Scrollbar) e.getSource()).getValue();
			if(value/10 == 0) valueString = "00"+value;
			else if(value/100 == 0) valueString = "0"+value;
			else valueString = ""+value;

			if((Scrollbar) e.getSource() == rSlider){
				rValue = value;
				rLabel.setText("R("+valueString+")");
				rLabel.setFont(MONOSPACED);
			}else if((Scrollbar) e.getSource() == gSlider){
				gValue = value;
				gLabel.setText("G("+valueString+")");
				gLabel.setFont(MONOSPACED);
			}else{
				bValue = value;
				bLabel.setText("B("+valueString+")");
				bLabel.setFont(MONOSPACED);
			}

			brushColor = new Color(rValue,gValue,bValue);

			Graphics gPreview = previewCanvas.getGraphics();
			Graphics g2dPreview = (Graphics2D) gPreview;
			g2dPreview.setColor(Color.WHITE);
			g2dPreview.fillRect(0,0,50,50);
			g2dPreview.setColor(brushColor);
			g2dPreview.fillRect(0,0,brushStroke,brushStroke);
		}
	}

	class ButtonHandler implements ActionListener{
		public void actionPerformed(ActionEvent event){

			Button source = (Button) event.getSource();
			if(source.getLabel().equals("Clear Canvas")){

				Graphics g = canvas.getGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0,0, 500,500);
				g.setColor(Color.BLACK);
				g.drawString("- press '+'/'-' to adjust brush size ",50,40);
				g.drawString("- Hold shift to change brush shape",50,55);
				g.drawString("- Click on the colour preview to fill background",50,70);
				g.drawString("- THIS MESSAGE WILL DISAPPEAR ONCE YOU START DRAWING",50,85);

				Graphics offscreen = img.getGraphics();
				offscreen.setColor(Color.WHITE);
				offscreen.fillRect(0,0,500,500);


			}else if(source.getLabel().equals("Reset Brush")){

				rSlider.setValue(0);
				gSlider.setValue(0);
				bSlider.setValue(0);

				brushColor = new Color(0,0,0);

				rLabel.setText("R(000)");
				gLabel.setText("G(000)");
				bLabel.setText("B(000)");

				brushStroke = DEFAULT_BRUSH_STROKE;

				Graphics gPreview = previewCanvas.getGraphics();
				Graphics g2dPreview = (Graphics2D) gPreview;
				g2dPreview.setColor(Color.WHITE);
				g2dPreview.fillRect(0,0,50,50);
				g2dPreview.setColor(brushColor);
				g2dPreview.fillRect(0,0,brushStroke,brushStroke);

			}else{}
		}
	}	

	class DrawingCanvas extends Canvas{

		public void paint(Graphics g){
			if(paintCountFlag <= 1){

				Dimension d = this.getSize();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, d.width, d.height);
				g.setColor(Color.BLACK);
				g.drawString("- press '+'/'-' to adjust brush size ",50,40);
				g.drawString("- Hold shift to change brush shape",50,55);
				g.drawString("- Click on the colour preview to fill background",50,70);
				g.drawString("- THIS MESSAGE WILL DISAPPEAR ONCE YOU START DRAWING",50,85);
				paintCountFlag++;

			} else{
				g.drawImage(img,0,0,this);
			}

		}

		public void update(){
			paint(canvas.getGraphics());
		}
	}

	class PreviewCanvas extends Canvas{
		public void paint(Graphics g){


			Graphics gPreview = previewCanvas.getGraphics();
			Graphics g2dPreview = (Graphics2D) gPreview;
			g2dPreview.setColor(Color.WHITE);
			g2dPreview.fillRect(0,0,50,50);
			g2dPreview.setColor(brushColor);
			g2dPreview.fillRect(0,0,brushStroke,brushStroke);

		}

		public void update(Graphics g){
			paint(g);
		}
	}

	protected void setGUI(){
		setLayout(new BorderLayout());

		southMainPanel = new Panel();
		add(southMainPanel, BorderLayout.SOUTH);
		centerMainPanel = new Panel();
		add(centerMainPanel, BorderLayout.CENTER);

		// setting up the canvas
		centerMainPanel.add(canvas);
		canvas.setSize(500,500);
		canvas.paint(canvas.getGraphics());

		// settings panel
		southMainPanel.setLayout(new BorderLayout());
		// r,g,b sliders
		rSlider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 256);
		gSlider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 256);
		bSlider = new Scrollbar(Scrollbar.HORIZONTAL, 0, 0, 0, 256);

		sliderPanel = new Panel();
		sliderPanel.setLayout(new GridLayout(3,1,2,2));

		Panel rPanel = new Panel();
		rPanel.setLayout(new BorderLayout());
		rPanel.add(rSlider, BorderLayout.CENTER);
		rLabel = new Label("R(000)");
		rLabel.setFont(MONOSPACED);
		rPanel.add(rLabel, BorderLayout.WEST);

		Panel gPanel = new Panel();
		gPanel.setLayout(new BorderLayout());
		gPanel.add(gSlider, BorderLayout.CENTER);
		gLabel = new Label("G(000)");
		gLabel.setFont(MONOSPACED);
		gPanel.add(gLabel, BorderLayout.WEST);

		Panel bPanel = new Panel();
		bPanel.setLayout(new BorderLayout());
		bPanel.add(bSlider, BorderLayout.CENTER);
		bLabel = new Label("B(000)");
		bLabel.setFont(MONOSPACED);
		bPanel.add(bLabel, BorderLayout.WEST);

		sliderPanel.add(rPanel);
		sliderPanel.add(gPanel);
		sliderPanel.add(bPanel);
		southMainPanel.add(sliderPanel, BorderLayout.CENTER);

		// preview Panel
		previewPanel = new Panel();
		southMainPanel.add(previewPanel, BorderLayout.EAST);

		previewPanel.add(previewCanvas);
		previewCanvas.setSize(50,50);

		// buttons
		buttonPanel = new Panel();
		buttonPanel.setLayout(new GridLayout(1,2,5,2));

		// clear canvas button
		clearCanvasButton = new Button("Clear Canvas");
		buttonPanel.add(clearCanvasButton);

		resetBrushButton = new Button("Reset Brush");
		buttonPanel.add(resetBrushButton);

		southMainPanel.add(buttonPanel, BorderLayout.SOUTH);
		Graphics offscreen = img.getGraphics();
		offscreen.setColor(Color.WHITE);
		offscreen.fillRect(0,0,500,500);
	}

}




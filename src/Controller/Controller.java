package Controller;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import Model.Board;
import Model.Model;
import Service.Constants;
import View.BoardView;

/**
 * Created by juan on 23/05/17.
 */
public class Controller {
	private static Board board;
	private static Model model;
	private static BoardView boardView;
	private static int playerN; // this variable is not updated by all controller functions thus becoming useless after initiation.
	private static int visual;
	private static int file;
	private static int tree;

	public static void main(String[] args){

		ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
		try {
			if (argsList.size() < 3)
				throw new IllegalArgumentException();
			else {
				visual = argsList.indexOf("-visual");
				file = argsList.indexOf("-file");

				if ((visual == -1 && file == -1) || (visual != -1 && file != -1)) {
					throw new IllegalArgumentException();
				}

				if (file!=-1){
					String filename = argsList.get(file+1);
					File fileHandler = new File(filename);
					if(!fileHandler.exists())
						throw new FileNotFoundException();
					Board startingBoard = readBoard(fileHandler);
					if(startingBoard == null)
						throw new IOException();
				}

				int player = argsList.indexOf("-player");
				playerN=0;

				if ((player != -1 && file == -1) || (file != -1 && player == -1)){
					throw new IllegalArgumentException();
				}

				if (player!=-1){
					try {
						playerN = Integer.parseInt(argsList.get(player + 1));
						if (playerN!=1&&playerN!=2)
							throw new NumberFormatException();
					}
					catch (NumberFormatException e){
						throw new IllegalArgumentException();
					}
				}

				int maxtime = argsList.indexOf("-maxtime");
				int maxtimeN=0;
				int depth = argsList.indexOf("-depth");
				int depthN=0;

				if ((maxtime != -1 && depth != -1) || (maxtime == -1 && depth == -1)){
					throw new IllegalArgumentException();
				}

				if(maxtime!=-1){
					try {
						maxtimeN = Integer.parseInt(argsList.get(maxtime + 1));
						if (maxtimeN<=0)
							throw new NumberFormatException();
						Constants.maxTime = maxtimeN;
					}
					catch (NumberFormatException e){
						throw new IllegalArgumentException();
					}
				}

				if(depth!=-1){
					try {
						depthN = Integer.parseInt(argsList.get(depth + 1));
						if(depthN<=0)
							throw new NumberFormatException();
						Constants.depth = depthN;
					}
					catch (NumberFormatException e){
						throw new IllegalArgumentException();
					}
				}

				int prune = argsList.indexOf("-prune");
				tree = argsList.indexOf("-tree");

				if (prune!=-1)
					Constants.prune = true;

				if(tree!=-1 && file!=1)
					Constants.dotTree = true;
			}
		}
		catch (IllegalArgumentException e){
			System.out.println("Invalid parameters, try:\njava -jar tpe.jar (-visual | -file archivo -player n) (-maxtime n | -depth n) [-prune] [-tree]\n");
			return;
		}
		catch (IOException e){
			System.out.println("Invalid file\n");
			return;
		}

		// Parsed values:
		//     visual : -1 is false, !=-1 is true
		//     file : -1 is false, !=-1 is true
		//     playerN : 1 or 2 representing the players turn
		//     maxtimeN : Saved in constants
		//     depthN : Saved in constants
		//     prune : Saved in constants
		//     tree : -1 is false, !=-1 is true

		board = new Board();
		boardView = new BoardView(board);
		model = new Model(board);

		//initializes the app window.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					boardView.initFrame();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		if (visual != -1) {
			playerN = 1;
			model.gameLoop();
		}
		else{
			model.executeFileMode(board,playerN);
		}



	}

	/**The controller attempts to add a stone to the board. If the model validates the
	 * attempt it will update both the current player and the view accordingly, if not it will return false.
	 * @param x the x coordinate where the stone is to be potentially placed.
	 * @param y the y coordinate where the stone is to be potentially placed.
	 * @param player the player who is attempting to place the stone.
	 * @return true if the model validated the placement, false otherwise.
	 * */
	public static boolean placingAttempt(int x, int y, int player){
		System.out.println("reached controller");
		System.out.println(x);
		System.out.println(y);
		if(board.addPiece(x,y,player)) {
			board.nextPlayer();
			boardView.setStone(x*13 + y);
			boardView.nextPlayer();
			return true;
		}
		else
			return false;

	}
	public static void updateView(Board board){
		boardView.placeStones(board);
	}

	/**The controller tells the model that a player has passed and updates the player.
	 * */
	public static void pass(){
		board.pass(playerN);
		boardView.nextPlayer();
	}


	/**The controller sets a stone of the current player in the x,y coordinates.
	 * @param x the x coordinate where the stone is to be potentially placed.
	 * @param y the y coordinate where the stone is to be potentially placed.
	 * @return true if the stone could be placed, false otherwise.
	 * */
	public static boolean placeStone(int x, int y){
		int index = x*13 +y;
		return boardView.setStone(index);
	}
	/**The controller will attempt to remove a stone from the view.
	 * @param x the x coordinate where the stone is to be potentially placed.
	 * @param y the y coordinate where the stone is to be potentially placed.
	 * @return true if the stone could be removed, false otherwise.
	 * */
	public static boolean removeStone(int x, int y){
		int index = x*13 + y;
		if( index >= 169 || index <0)
			throw new IllegalArgumentException("index out of bounds");
		return boardView.removeStone(x*13 + y);
	}

	// no se que hace, la deje por las dudas.
    public static Board waitForPlayerMove(Board board){
        return new Board();//dummy para q compile

        // cuando el jugador pasa devuelve null
    }

    public static Board readBoard(File file){
      Board board = new Board();
    	int c;
    	int xPos = 0;
    	int yPos = 0;

    	try {
    		FileInputStream fileIn = new FileInputStream(file);
    		while ((c = fileIn.read()) != -1) {
    			if(c == '\n'){
    				yPos++;
    				xPos = 0;
    			}
    			else {
    				if(c != ' ')
    					board.addPiece(xPos, yPos, c-'0');
    				xPos++;
    			}
    		}
				fileIn.close();

    	} catch (IOException i) {
    		board = null;
    	}

    	return board;
    }
}

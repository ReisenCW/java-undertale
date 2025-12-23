package undertale;
import java.io.File;

import undertale.GameMain.Game;

public class Main {
	   public static void main(String[] args) {
		   System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
		   Game.getInstance().run();
	   }
}
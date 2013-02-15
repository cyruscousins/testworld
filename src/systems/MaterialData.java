package systems;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


//Keep track of parameters about the materials that things are made of.
public class MaterialData{
  //Static (Materials manager)
  public static HashMap<String, MaterialData> materials;
  
  public static HashMap<String, List<String>> materialTypes;
  
  public static MaterialData getMaterial(String name){
      //TODO test for null, throw error or return new blank material.
      MaterialData mat = materials.get(name);
//      if(mat == null){
//          System.err.println("ERROR: Material \"" + name + "\" not found in database.  Terminating.");
//          System.exit(1);
//      }
      return mat;
//      return materials.get(name);
  }
  
  public static MaterialData getRandomMaterial(String type){
      List<String> possibilities = materialTypes.get(type);
      return materials.get(possibilities.get(FastMath.rand.nextInt(possibilities.size()))); //TODO use our own rand!  Thread speed.
  }
  
  //Material data
  public String name;
  HashMap<String, Float> data;
  
  MaterialData(String name){
    this.name = name;
    data = new HashMap<String, Float>();
  }
  MaterialData(String name, HashMap<String, Float> data){
    this.name = name;
    this.data = data;
  }
  
  public float getParameter(String name){
      Float val = data.get(name);
      if(val == null) return 1;
      return val.floatValue();
  }
  
  public ColorRGBA getColor(){
      return new ColorRGBA(getParameter("Color R"), getParameter("Color G"), getParameter("Color B"), 1);
  }
  
  public static void init(){
        materials = new HashMap<String, MaterialData>();
        materialTypes = new HashMap<String, List<String>>();
        try {
            loadMaterialDatabase("res/matdata/wood.csv");
            loadMaterialDatabase("res/matdata/metal.csv");
            loadMaterialDatabase("res/matdata/stone.csv");
        } catch (IOException ex) {
            Logger.getLogger(MaterialData.class.getName()).log(Level.SEVERE, null, ex);
        }
//        staticInit();
  }
  
  //Material database loader
  public static final String splitStr = "\\s*,\\s";
  public static void loadMaterialDatabase(String filePath) throws IOException{

      System.out.println("Reading materials database at " + filePath);

      BufferedReader in = new BufferedReader(new FileReader(filePath));
	
      String[] parameterNames = in.readLine().split(",");
      //parameterNames[0] is the list name, the rest of the array is parameter names.
      
      List list = materialTypes.get(parameterNames[0]);
      if(list == null){
          list = new ArrayList<String>();
          materialTypes.put(parameterNames[0], list);
      }
      for(String nextLine = in.readLine(); nextLine != null; nextLine = in.readLine())
      {
	  String[] toks = nextLine.split(",");
          //0 is name, rest are parameter values.
          
          list.add(toks[0]);
          MaterialData mat = new MaterialData(toks[0]);
          
	  for(int i = 1; i < toks.length; i++){
              mat.data.put(parameterNames[i], Float.parseFloat(toks[i]));
	  }
          
          materials.put(toks[0], mat);
      }
   }
  
  
   //Old test code
  
  //Static Test Initializer
  public static void staticInit(){
//    materials = new HashMap<String, MaterialData>();
    
    String[] names = new String[]{"Flaxen", "Leather", "Stone", "Copper", "Steel"};
    
    String[][] pnames = new String[][]{
        new String[]{"Flamable", "Durable", "Hard", "Density"},
        new String[]{"Flamable", "Durable", "Hard", "Density"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Malleability"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Malleability"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Malleability", "Insulation"}
    };
    float[][] pvals = new float[][]{
        new float[]{4f,  .75f, .1f,  .1f},
        new float[]{3f, 1.1f,  .3f,  .2f},
        new float[]{0f, .5f,   .3f,   2f, .01f},
        new float[]{0f, 1.8f,  .7f, 1.4f, .75f},
        new float[]{0f,   4f, 1.5f, 1.2f, .6f, 2f}
    };
    addParams(names, pnames, pvals);
    
    names = new String[]{"Oak", "Maple", "Pine", "Yew"};
    
    pnames = new String[][]{
        new String[]{"Flamable", "Durable", "Hard", "Density", "Regularity"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Regularity"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Regularity"},
        new String[]{"Flamable", "Durable", "Hard", "Density", "Regularity"},
    };
    pvals = new float[][]{
        new float[]{  2f, 1.2f, 1.2f,  .1f, 1.5f},
        new float[]{2.5f, 1.3f, 1.3f,  .3f, 1.4f},
        new float[]{1.5f,  .5f,  .5f,  .3f, 1.2f},
        new float[]{1.8f, 1.4f, 1.4f, 1.4f, 2.8f},
    };
    addParams(names, pnames, pvals);
    
  }
  public static void addParams(String[] names, String[][] pnames, float[][] pvals){
      
    for(int i = 0; i < names.length; i++){
      HashMap<String, Float> data = new HashMap<String, Float>();
      for(int j = 0; j < pnames[i].length; j++){
        data.put(pnames[i][j], pvals[i][j]);
      }
      materials.put(names[i], new MaterialData(names[i], data));
    }
  }
  
}

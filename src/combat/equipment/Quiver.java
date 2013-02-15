package combat.equipment;

import com.jme3.math.FastMath;
import systems.MaterialData;

/**
 * Arrows go in, arrows come out.  Randomly
 */
public class Quiver {
  ArrowData[] arrows;
  int qSize;
  int arrowTop = -1;
  
  //Create a quiver and supply the maximum size
  public Quiver(int size){
      arrows = new ArrowData[size];
      this.qSize = size;
  }
  
  //returns false on failure
  public boolean addArrow(ArrowData narrow){
    if(arrowTop + 1 == qSize){
      return false;
    }
    
    //randomize quiver (put every third arrow in randomly)
    arrows[++arrowTop] = narrow;
    if((arrowTop & 3) == 3) flipArrows(arrowTop, FastMath.rand.nextInt(arrowTop)); //flip a third of the new arrows (but never the first one).
    return true;
  }
  //returns false on failure (without adding anything){
  public boolean addArrows(ArrowData[] a){
    if(arrowTop + a.length >= qSize) return false;
    
    for(int i = 0; i < a.length; i++){
      arrows[++arrowTop] = a[i];
      if((i & 3) == 3) flipArrows(i, FastMath.rand.nextInt(i)); //flip a third of the new arrows (but never the first one).
    }
    arrowTop--;
    return true;
  }
  //Flips two ararows, used for randomization of quiver
  private final void flipArrows(int a, int b){
    ArrowData temp = arrows[a];
    arrows[a] = arrows[b];
    arrows[b] = temp;
  }
  //Throws exception on empty quiver.
  public ArrowData popArrow(){
    ArrowData a = arrows[arrowTop];
    arrows[arrowTop--] = null;
    return a;
  }
  
  public boolean notEmpty(){
    return arrowTop >= 0;
  }
  
  public int arrowCount(){
    return arrowTop + 1;
  }
  
  public static Quiver randQ(){
      Quiver q = new Quiver(100);
      MaterialData head, shaft;
      for(int i = 0; i < 50; i++){
          if(FastMath.rand.nextFloat() < .25f){
              head = MaterialData.getRandomMaterial("Stone");
          }
          else{
              head = MaterialData.getRandomMaterial("Metal");
          }
          
          if(FastMath.rand.nextFloat() < .25f){
              shaft = MaterialData.getRandomMaterial("Metal");
          }
          else{
              shaft = MaterialData.getRandomMaterial("Wood");
          }
          q.addArrow(new ArrowData(head, shaft, 1f + FastMath.rand.nextFloat()));
      }
      return q;
  }
}
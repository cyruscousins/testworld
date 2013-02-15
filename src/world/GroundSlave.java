/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.jme3.math.Vector3f;

/**
 *
 * @author ccousins
 */
public class GroundSlave implements Runnable{
    GroundManager master;
    int a, b;

    public GroundSlave(GroundManager master, int a, int b) {
        this.master = master;
        this.a = a;
        this.b = b;
    }
    
    public boolean finished; //this might be atomic.
    public Hexile result;
    public void run(){
        result = new Hexile(new Vector3f((GroundManager.AX * a + GroundManager.BX * b) * master.hexSize, 0, (master.AZ * a + master.BZ * b) * master.hexSize), a, b, master.heightMap, master.hexSize, master.lodMax);
        finished = true;
    }
}

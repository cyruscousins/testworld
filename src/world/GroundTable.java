/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

/**
 *
 * @author ccousins
 */
public class GroundTable {
    Hexile[] data;
    int[] keys;
    int size;
    
    public GroundTable(int size){
        data = new Hexile[size];
        keys = new int[size];
        this.size = size;
    }
    public Hexile get(int a, int b){
        return null;
    }
    public void put(Hexile hex){
        
    }
    private int hash(int a, int b){
        return (a * 5 + b * 13) % size;
    }
}

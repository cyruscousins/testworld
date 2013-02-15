/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package combat.equipment;

/**
 *
 * @author ccousins
 */
public class Bow {
    public float powerEfficiency;
    public float maxPower;
    public float accuracy;
    public float weight;

    public Bow(float powerEfficiency, float maxPower, float accuracy, float weight) {
        this.powerEfficiency = powerEfficiency;
        this.maxPower = maxPower;
        this.accuracy = accuracy;
        this.weight = weight;
    }
}

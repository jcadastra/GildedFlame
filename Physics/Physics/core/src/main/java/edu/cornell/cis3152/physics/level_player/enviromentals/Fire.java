package edu.cornell.cis3152.physics.level_player.enviromentals;

public class Fire {

    /**
     * Boolean value if the fire spreads to other sources or stays constant
     */
    private boolean spreads;

    /**
     * General fire usuage, can either spread or not depending on needs
     * If spreads, then also destructive
     */
    public Fire() {

    }
}

/**
 * TODO:
 *
 * Given a fire, needs to burn
 * so a singular fire needs to have a light source and partcile generator
 *
 * one or spreading?
 * one i think
 * given a polygon, randomly place points within it by triangulation
 *
 * need fire controller for spreading
 *      can have dictionary from body to n distribution
 * fire to generate things
 *
 * for each block on fire not all lit
 *      get point closet to next fire on blcok, light it with material based lighting timer
 *          add contact hangler that if fire git burnable block but is not on fire then add to fire
 *      if all points then set off internal material lighitng timer
 * upon expiration of internal block lighting timer, destroy
 */

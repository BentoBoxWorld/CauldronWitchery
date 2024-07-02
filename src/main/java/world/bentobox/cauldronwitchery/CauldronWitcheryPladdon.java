//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class CauldronWitcheryPladdon extends Pladdon
{
    private Addon addon;
    @Override
    public Addon getAddon()
    {
        if (addon == null) {
            addon = new CauldronWitcheryAddon();
        }
        return addon;
    }
}

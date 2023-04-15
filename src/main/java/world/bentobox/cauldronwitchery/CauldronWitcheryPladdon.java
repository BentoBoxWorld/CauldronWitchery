//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class CauldronWitcheryPladdon extends Pladdon
{
    @Override
    public Addon getAddon()
    {
        return new CauldronWitcheryAddon();
    }
}

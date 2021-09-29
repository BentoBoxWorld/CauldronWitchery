package world.bentobox.cauldronwitchery.panels.admin;


import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * This class opens GUI that shows bundle view for user.
 */
public class EditPlayerPanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param panel Parent Panel
     */
    private EditPlayerPanel(CommonPanel panel,
        User user)
    {
        super(panel);
        this.viewUser = user;
    }


    /**
     * This method is used to open UserPanel outside this class. It will be much easier to open panel with single method
     * call then initializing new object.
     *
     * @param panel Parent Panel
     */
    public static void open(CommonPanel panel,
        User user)
    {
        new EditPlayerPanel(panel, user).build();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    protected void build()
    {
        // Do not enable this GUI if there is an issue with getting data.
        if (this.viewUser == null)
        {
            Utils.sendMessage(this.user, this.user.getTranslation(
                Constants.ERRORS + "no-player-data"));
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    private final User viewUser;
}

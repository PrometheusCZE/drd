package cz.stechy.drd.gui.shop;

import static org.testfx.api.FxAssert.verifyThat;

import cz.stechy.drd.GUITestBase;
import cz.stechy.drd.R.Translate;
import cz.stechy.drd.pages.shop.ShopPage;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TitledPane;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Třída obsahující Gui testy pro screen {@link cz.stechy.drd.controller.shop.ShopController1}
 */
@RunWith(JUnitParamsRunner.class)
public final class ShopGuiTest extends GUITestBase {

    // region Constants

    private static final String PANE_WEAPON_MELE = "#paneWeaponMele";
    private static final String PANE_WEAPON_RANGED = "#paneWeaponRanged";
    private static final String PANE_ARMOR = "#paneArmor";
    private static final String PANE_GENERAL = "#paneGeneral";
    private static final String PANE_BACKPACK = "#paneBackpack";

    private static final String BTN_ADD_ITEM = "#btnAddItem";
    private static final String BTN_REMOVE_ITEM = "#btnRemoveItem";
    private static final String BTN_EDIT_ITEM = "#btnEditItem";
    private static final String BTN_SYNCHRONIZE = "#btnSynchronize";
    private static final String BTN_TOGGLE_ONLINE = "#btnToggleOnline";

    // endregion

    // region Variables

    private ShopPage shopPage;

    // endregion

    // region Private methods

    @Before
    public void beforeEachTest() throws Exception {
        super.beforeEachTest();
        this.shopPage = (ShopPage) super.mainPage.showShop();
    }


    private Object[] parametersForButtonTooltip() {
        return new Object[] {
            new Object[] {BTN_ADD_ITEM, Translate.SHOP_ITEM_ADD},
            new Object[] {BTN_REMOVE_ITEM, Translate.SHOP_ITEM_REMOVE},
            new Object[] {BTN_EDIT_ITEM, Translate.SHOP_ITEM_EDIT},
            new Object[] {BTN_SYNCHRONIZE, Translate.SHOP_ITEM_SYNCHRONIZE},
            new Object[] {BTN_TOGGLE_ONLINE, Translate.FIREBASE_TOGGLE_ONLINE_DATABASE},
        };
    }

    private Object[] parametersForPaneTranslation() {
        return new Object[] {
            new Object[] {PANE_WEAPON_MELE, Translate.ITEM_TYPE_WEAPON_MELE},
            new Object[] {PANE_WEAPON_RANGED, Translate.ITEM_TYPE_WEAPON_RANGED},
            new Object[] {PANE_ARMOR, Translate.ITEM_TYPE_ARMOR},
            new Object[] {PANE_GENERAL, Translate.ITEM_TYPE_GENERAL},
            new Object[] {PANE_BACKPACK, Translate.ITEM_TYPE_BACKPACK}
        };
    }

    // endregion

    @Test
    @Parameters(method = "parametersForButtonTooltip")
    public void testButtonTooltip(String identifier, String tooltip) throws Exception {
        verifyThat(identifier, (ButtonBase button) -> {
            return button.getTooltip().getText().equals(shopPage.getTitleFromBundle(tooltip));
        });
    }

    @Test
    @Parameters(method = "parametersForPaneTranslation")
    public void testPaneTranslation(String identifier, String translate) throws Exception {
        verifyThat(identifier, (TitledPane pane) -> {
            return pane.getText().equals(shopPage.getTitleFromBundle(translate));
        });
    }


}
package randomapps.g_contactsqr;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import de.robv.android.xposed.IXposedHookLoadPackage;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class main implements IXposedHookLoadPackage {

    private int menuQRCodeId = 0;
    private Menu menu = null;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.google.android.contacts"))
            return;
        XposedBridge.log("we are in contacts");

        findAndHookMethod("com.google.android.apps.contacts.quickcontact.QuickContactActivity", lpparam.classLoader, "onOptionsItemSelected", MenuItem.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                MenuItem menuItem = (MenuItem) param.args[0];
                if (menuItem.getItemId() == 12345) {
                    param.setResult(false);

                    String fakeMenuItemString = menuItem.toString();

                    menu.removeItem(menuQRCodeId);
                    menu.removeItem(12345);
                    // XposedBridge.log("removed original and fake QR option");
                    MenuItem OriginalLikeMenuItem = menu.add(12345, menuQRCodeId, 7, fakeMenuItemString);
                    // XposedBridge.log("added QR item such that contacts thinks it's theirs");

                    //XposedBridge.log("now calling method again with fake menu item");
                    XposedHelpers.callMethod(param.thisObject, "onOptionsItemSelected", OriginalLikeMenuItem);
                }
            }
        });


        findAndHookMethod("com.google.android.apps.contacts.quickcontact.QuickContactActivity", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // XposedBridge.log("attempting to add QR");
                // XposedBridge.log("original result is " + String.valueOf(param.getResult()));
                final Context context = (Context) AndroidAppHelper.currentApplication();
                // XposedBridge.log("the package of the context is " + context.getPackageName());

                menuQRCodeId = context.getResources().getIdentifier("menu_qr_code", "id", AndroidAppHelper.currentPackageName());
                // XposedBridge.log("id of menu_qr_code is " + String.valueOf(menu_QR_code_id));
                menu = (Menu) param.args[0];
                // XposedBridge.log("menu is " + menu.toString());
                MenuItem TheirQRItem = menu.findItem(menuQRCodeId);
                // XposedBridge.log("the found item is " + QR_Item.toString());

                MenuItem myQRItem = menu.add(12345, 12345, 7, TheirQRItem.toString());
            }
        });
    }
}

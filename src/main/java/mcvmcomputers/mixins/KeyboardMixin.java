package mcvmcomputers.mixins;

import static mcvmcomputers.client.ClientMod.*;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mcvmcomputers.client.ClientMod;
import mcvmcomputers.client.gui.GuiFocus;
import mcvmcomputers.client.gui.setup.pages.SetupPageUnfocusBinding;
import mcvmcomputers.client.utils.KeyConverter;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class KeyboardMixin {
	@Inject(at = @At("HEAD"), method = "onKey")
	public void onKey(long window, int key, int scancode, int i, int j, CallbackInfo ci) {
		MinecraftClient mcc = MinecraftClient.getInstance();
		 if (window == mcc.getWindow().getHandle()) {
			 if(ClientMod.vmTurnedOn && mcc.currentScreen instanceof GuiFocus) {
				 if(i != 2) {
					 ClientMod.vmKeyboardScancodes.addAll(KeyConverter.toVBKey(key, i));
				 }
			 }else if(SetupPageUnfocusBinding.changeBinding) {
				 if(i == GLFW.GLFW_PRESS) {
					 if(getKeyName(key) != null) {
						 switch (SetupPageUnfocusBinding.bindingToBeChangedNum) {
							 case 1 -> glfwUnfocusKey1 = key;
							 case 2 -> glfwUnfocusKey2 = key;
							 case 3 -> glfwUnfocusKey3 = key;
							 case 4 -> glfwUnfocusKey4 = key;
						 }
						 SetupPageUnfocusBinding.bindingJustChanged = true;
						 SetupPageUnfocusBinding.changeBinding = false;
					 }
				 }
			 }
		 }
	}
}

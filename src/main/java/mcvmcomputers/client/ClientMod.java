package mcvmcomputers.client;

import io.netty.buffer.Unpooled;
import mcvmcomputers.MainMod;
import mcvmcomputers.client.entities.render.*;
import mcvmcomputers.client.gui.GuiCreateHarddrive;
import mcvmcomputers.client.gui.GuiFocus;
import mcvmcomputers.client.gui.GuiPCEditing;
import mcvmcomputers.client.tablet.TabletOS;
import mcvmcomputers.entities.EntityDeliveryChest;
import mcvmcomputers.entities.EntityItemPreview;
import mcvmcomputers.entities.EntityList;
import mcvmcomputers.entities.EntityPC;
import mcvmcomputers.item.OrderableItem;
import mcvmcomputers.networking.PacketList;
import mcvmcomputers.utils.TabletOrder;
import mcvmcomputers.utils.TabletOrder.OrderStatus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.glfw.GLFW;
import org.virtualbox_7_0.ISession;
import org.virtualbox_7_0.IVirtualBox;
import org.virtualbox_7_0.VirtualBoxManager;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static mcvmcomputers.utils.MVCUtils.registerEntityRenderer;

public class ClientMod implements ClientModInitializer{
	public static final OutputStream discardAllBytes = new OutputStream() { @Override public void write(int b) {} };
	public static Map<UUID, Identifier> vmScreenTextures;
	public static Map<UUID, NativeImage> vmScreenTextureNI;
	public static Map<UUID, NativeImageBackedTexture> vmScreenTextureNIBT;
	public static EntityItemPreview thePreviewEntity;
	public static boolean vmTurnedOn;
	public static boolean vmTurningOff;
	public static boolean vmTurningOn;
	public static ISession vmSession;
	
	public static int maxRam = 8192;
	public static int videoMem = 256;
	
	public static VirtualBoxManager vbManager;
	public static IVirtualBox vb;
	
	public static Process vboxWebSrv;
	public static Thread vmUpdateThread;
	public static byte[] vmTextureBytes;
	public static int vmTextureBytesSize;
	public static boolean failedSend;
	
	public static double mouseLastX = 0;
	public static double mouseLastY = 0;
	public static double mouseCurX = 0;
	public static double mouseCurY = 0;
	public static int mouseDeltaScroll;
	public static boolean leftMouseButton;
	public static boolean middleMouseButton;
	public static boolean rightMouseButton;
	public static List<Integer> vmKeyboardScancodes = new ArrayList<>();
	public static boolean releaseKeys = false;
	public static File vhdDirectory;
	public static File isoDirectory;
	public static int latestVHDNum = 0;
	public static TabletOS tabletOS;
	public static TabletOrder myOrder;
	public static int vmEntityID = -1;
	
	public static Thread tabletThread;
	
	public static float deltaTime;
	public static long lastDeltaTimeTime;
	
	public static int glfwUnfocusKey1;
	public static int glfwUnfocusKey2;
	public static int glfwUnfocusKey3;
	public static int glfwUnfocusKey4;
	
	static {
		if(SystemUtils.IS_OS_MAC) {
			glfwUnfocusKey1 = GLFW.GLFW_KEY_LEFT_ALT;
			glfwUnfocusKey2 = GLFW.GLFW_KEY_RIGHT_ALT;
		}else {
			glfwUnfocusKey1 = GLFW.GLFW_KEY_LEFT_CONTROL;
			glfwUnfocusKey2 = GLFW.GLFW_KEY_RIGHT_CONTROL;
		}
		glfwUnfocusKey3 = GLFW.GLFW_KEY_BACKSPACE;
		glfwUnfocusKey4 = -1;
	}
	
	public static EntityDeliveryChest currentDeliveryChest;
	public static EntityPC currentPC;
	
	public static String getKeyName(int key) {
		if (key < 0) {
			return "None";
		}else {
			return glfwKey(key);
		}
	}
	
	private static String glfwKey(int key) {
		return switch (key) {
			case GLFW.GLFW_KEY_LEFT_CONTROL -> "L Control";
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R Control";
			case GLFW.GLFW_KEY_RIGHT_ALT -> "R Alt";
			case GLFW.GLFW_KEY_LEFT_ALT -> "L Alt";
			case GLFW.GLFW_KEY_LEFT_SHIFT -> "L Shift";
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R Shift";
			case GLFW.GLFW_KEY_ENTER -> "Enter";
			case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
			case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock";
			case GLFW.GLFW_KEY_TAB -> "Tab";
			default -> GLFW.glfwGetKeyName(key, 0);
		};
	}
	
	public static void getVHDNum() throws NumberFormatException, IOException {
		File f = new File(vhdDirectory.getParentFile(), "vhdnum");
		if(f.exists()) {
			latestVHDNum = Integer.parseInt(Files.readAllLines(f.toPath()).get(0));
		}
	}
	
	public static void increaseVHDNum() throws IOException {
		latestVHDNum++;
		File f = new File(vhdDirectory.getParentFile(), "vhdnum");
		if(f.exists()) {
			f.delete();
		}
		f.createNewFile();
		FileWriter fw = new FileWriter(f);
		fw.append("").append(String.valueOf(latestVHDNum));
		fw.flush();
		fw.close();
	}
	
	public static void generatePCScreen() {
		MinecraftClient mcc = MinecraftClient.getInstance();
		if(mcc.player == null) {
			return;
		}
		if(vmTextureBytes != null) {
			if(vmScreenTextures.containsKey(mcc.player.getUuid())) {
				MinecraftClient.getInstance().getTextureManager().destroyTexture(vmScreenTextures.get(mcc.player.getUuid()));
				vmScreenTextures.remove(mcc.player.getUuid());
			}
			
			Deflater def = new Deflater();
			def.setInput(vmTextureBytes);
			def.finish();
			byte[] deflated = new byte[vmTextureBytesSize];
			int sz = def.deflate(deflated);
			def.end();
			
			if(sz > 32766) {
				if(!failedSend){
					mcc.player.sendMessage(new TranslatableText("mcvmcomputers.screen_too_big_mp").formatted(Formatting.RED), false);
					failedSend = true;
				}
			}else {
				if(failedSend) {
					mcc.player.sendMessage(new TranslatableText("mcvmcomputers.screen_ok_mp").formatted(Formatting.GREEN), false);
					failedSend = false;
				}
				
				PacketByteBuf p = new PacketByteBuf(Unpooled.buffer());
				p.writeByteArray(Arrays.copyOfRange(deflated, 0, sz));
				p.writeInt(sz);
				p.writeInt(vmTextureBytesSize);
				ClientPlayNetworking.send(PacketList.C2S_SCREEN, p);
			}
			
			NativeImage ni = null;
			try {
				ni = NativeImage.read(new ByteArrayInputStream(vmTextureBytes));
			} catch (IOException ignored) {
			}
			if(ni != null) {
				if(vmScreenTextureNI.containsKey(mcc.player.getUuid())) {
					vmScreenTextureNI.get(mcc.player.getUuid()).close();
					vmScreenTextureNI.remove(mcc.player.getUuid());
				}
				if(vmScreenTextureNIBT.containsKey(mcc.player.getUuid())) {
					vmScreenTextureNIBT.get(mcc.player.getUuid()).close();
					vmScreenTextureNIBT.remove(mcc.player.getUuid());
				}
				vmScreenTextureNI.put(mcc.player.getUuid(), ni);
				NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
				vmScreenTextureNIBT.put(mcc.player.getUuid(), nibt);
				vmScreenTextures.put(mcc.player.getUuid(), MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("vm_texture", nibt));
			}
			vmTextureBytes = null;
		}
	}
	
	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(PacketList.S2C_SCREEN, (client, handler, buf, responseSender) -> {
			byte[] screen = buf.readByteArray();
			int compressedDataSize = buf.readInt();
			int dataSize = buf.readInt();
			UUID pcOwner = buf.readUuid();
			
			client.execute(() -> {
				MinecraftClient mcc = MinecraftClient.getInstance();
				if(!pcOwner.equals(Objects.requireNonNull(mcc.player).getUuid())) {
					if(ClientMod.vmScreenTextures.containsKey(pcOwner)) {
						mcc.getTextureManager().destroyTexture(ClientMod.vmScreenTextures.get(pcOwner));
						vmScreenTextures.remove(mcc.player.getUuid());
					}
					if(ClientMod.vmScreenTextureNI.containsKey(pcOwner)) {
						ClientMod.vmScreenTextureNI.get(pcOwner).close();
						vmScreenTextureNI.remove(mcc.player.getUuid());
					}
					if(ClientMod.vmScreenTextureNIBT.containsKey(pcOwner)) {
						ClientMod.vmScreenTextureNI.get(pcOwner).close();
						vmScreenTextureNIBT.remove(mcc.player.getUuid());
					}
					try {
						Inflater inf = new Inflater();
						inf.setInput(screen, 0, compressedDataSize);
						byte[] actualScreen = new byte[dataSize+1];
						int size = inf.inflate(actualScreen);
						inf.end();
						NativeImage ni = NativeImage.read(new ByteArrayInputStream(actualScreen, 0, size));
						NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
						ClientMod.vmScreenTextures.put(pcOwner, mcc.getTextureManager().registerDynamicTexture("pc_screen_mp", nibt));
						ClientMod.vmScreenTextureNI.put(pcOwner, ni);
						ClientMod.vmScreenTextureNIBT.put(pcOwner, nibt);
					} catch (IOException | DataFormatException e) {
						e.printStackTrace();
					}
				}
			});
		});
		
		ClientPlayNetworking.registerGlobalReceiver(PacketList.S2C_STOP_SCREEN, (client, handler, buf, responseSender) -> {
			UUID pcOwner = buf.readUuid();
			
			client.execute(() -> {
				MinecraftClient mcc = MinecraftClient.getInstance();
				if(ClientMod.vmScreenTextures.containsKey(pcOwner)) {
					mcc.getTextureManager().destroyTexture(ClientMod.vmScreenTextures.get(pcOwner));
					vmScreenTextures.remove(Objects.requireNonNull(mcc.player).getUuid());
				}
				if(ClientMod.vmScreenTextureNI.containsKey(pcOwner)) {
					ClientMod.vmScreenTextureNI.get(pcOwner).close();
					vmScreenTextureNI.remove(Objects.requireNonNull(mcc.player).getUuid());
				}
				if(ClientMod.vmScreenTextureNIBT.containsKey(pcOwner)) {
					ClientMod.vmScreenTextureNIBT.get(pcOwner).close();
					vmScreenTextureNIBT.remove(Objects.requireNonNull(mcc.player).getUuid());
				}
			});
		});
		
		ClientPlayNetworking.registerGlobalReceiver(PacketList.S2C_SYNC_ORDER, (client, handler, buf, responseSender) -> {
			int arraySize = buf.readInt();
			OrderableItem[] arr = new OrderableItem[arraySize];
			for(int i = 0;i<arraySize;i++) {
				arr[i] = (OrderableItem) Objects.requireNonNull(client.player).getInventory().getStack(i).getItem();
			}
			int price = buf.readInt();
			OrderStatus status = OrderStatus.values()[buf.readInt()]; //send ordinal
			
			client.execute(() -> {
				if(ClientMod.myOrder == null) {
					ClientMod.myOrder = new TabletOrder();
				}
				ClientMod.myOrder.price = price;
				ClientMod.myOrder.items = Arrays.asList(arr);
				ClientMod.myOrder.orderUUID = Objects.requireNonNull(client.player).getUuidAsString();
				ClientMod.myOrder.currentStatus = status;
			});
		});
	}
	
	@Override
	public void onInitializeClient() {
		MainMod.pcOpenGui = () -> MinecraftClient.getInstance().openScreen(new GuiPCEditing(currentPC));
		MainMod.hardDriveClick = () -> MinecraftClient.getInstance().openScreen(new GuiCreateHarddrive());
		MainMod.focus = () -> MinecraftClient.getInstance().openScreen(new GuiFocus());
		MainMod.deliveryChestSound = () -> {
			if(MinecraftClient.getInstance().getSoundManager().isPlaying(currentDeliveryChest.rocketSound)) {
				MinecraftClient.getInstance().getSoundManager().stop(currentDeliveryChest.rocketSound);
			}
		};
		
		registerClientPackets();
		
		vmScreenTextures = new HashMap<>();
		vmScreenTextureNI = new HashMap<>();
		vmScreenTextureNIBT = new HashMap<>();
		
		registerEntityRenderer(EntityList.ITEM_PREVIEW, ItemPreviewRender.class);
		registerEntityRenderer(EntityList.KEYBOARD, KeyboardRender.class);
		registerEntityRenderer(EntityList.MOUSE, MouseRender.class);
		registerEntityRenderer(EntityList.CRT_SCREEN, CRTScreenRender.class);
		registerEntityRenderer(EntityList.FLATSCREEN, FlatScreenRender.class);
		registerEntityRenderer(EntityList.WALLTV, WallTVRender.class);
		registerEntityRenderer(EntityList.PC, PCRender.class);
		registerEntityRenderer(EntityList.DELIVERY_CHEST, DeliveryChestRender.class);
	}

}

package mcvmcomputers.client.gui;

import io.netty.buffer.Unpooled;
import mcvmcomputers.client.ClientMod;
import mcvmcomputers.networking.PacketList;
import mcvmcomputers.utils.MVCUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import org.apache.commons.lang3.math.NumberUtils;
import org.virtualbox_7_0.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GuiCreateHarddrive extends Screen{
	private TextFieldWidget hddSize;
	private String status;
	private State currentState = State.MENU;
	private final Language lang = Language.getInstance();
	private Ext extension = Ext.vdi;
	private static final char COLOR_CHAR = (char) (0xfeff00a7);
	private ButtonWidget AA;
	private ButtonWidget BB;
	private final MinecraftClient minecraft = MinecraftClient.getInstance();

	public enum State{
		MENU,
		CREATE_NEW,
		SELECT_OLD
	}

	public enum Ext{
		vdi,
		vmdk
	}

	public GuiCreateHarddrive() {
		super(new TranslatableText("Create Harddrive"));
	}

	public String translation(String in) {
		return lang.get(in).replace("%c", ""+MVCUtils.COLOR_CHAR);
	}

	@Override
	public void init() {
		if(currentState == State.CREATE_NEW) {
			String s = "";
			if(hddSize != null) {
				s = hddSize.getText();
			}
			hddSize = new TextFieldWidget(this.textRenderer, this.width/2-150, this.height/2-10, 300, 20, new LiteralText(""));
			hddSize.setText(s);
			hddSize.setChangedListener(this::hddSizeUpdate);
			this.addDrawable(hddSize);
			this.hddSizeUpdate(hddSize.getText());
			AA = this.addDrawableChild(new ButtonWidget(this.width/2-150, this.height/2+25, 50, 20, new LiteralText("vdi"), (wdgt) -> extset(Ext.vdi)));
			AA.active = false;
			BB = this.addDrawableChild(new ButtonWidget(this.width/2-96, this.height/2+25, 50, 20, new LiteralText("vmdk"), (wdgt) -> extset(Ext.vmdk)));
			int newvhdWidth = textRenderer.getWidth(translation("mcvmcomputers.vhd_setup.newvhd"))+40;
			this.addDrawableChild(new ButtonWidget(this.width/2-(newvhdWidth/2), this.height/2+50, newvhdWidth, 20, new LiteralText(translation("mcvmcomputers.vhd_setup.newvhd")), this::createNew));
			int menuWidth = textRenderer.getWidth(translation("mcvmcomputers.vhd_setup.menu"))+40;
			this.addDrawableChild(new ButtonWidget(this.width - (menuWidth + 10), this.height - 30, menuWidth, 20, new LiteralText(translation("mcvmcomputers.vhd_setup.menu")), (wdgt) -> switchState(State.MENU)));
		}else if(currentState == State.MENU) {
			int newvhdWidth = textRenderer.getWidth(translation("mcvmcomputers.vhd_setup.newvhd"))+40;
			this.addDrawableChild(new ButtonWidget(this.width/2 - (newvhdWidth/2), this.height / 2 - 12, newvhdWidth, 20, new LiteralText(translation("mcvmcomputers.vhd_setup.newvhd")), (wdgt) -> switchState(State.CREATE_NEW)));
			int oldvhdWidth = textRenderer.getWidth(translation("mcvmcomputers.vhd_setup.oldvhd"))+40;
			this.addDrawableChild(new ButtonWidget(this.width / 2 - (oldvhdWidth / 2), this.height / 2 + 12, oldvhdWidth, 20, new LiteralText(translation("mcvmcomputers.vhd_setup.oldvhd")), (wdgt) -> switchState(State.SELECT_OLD)));
		}else {
			int lastY = 60;
			ArrayList<File> files = new ArrayList<>();
			for(File f : Objects.requireNonNull(ClientMod.vhdDirectory.listFiles())) {
				if(f.getName().endsWith(".vdi") || f.getName().endsWith(".vmdk")) {
					files.add(f);
				}
			}
			files.sort(Comparator.comparing(File::getName));
			for(File f : files) {
				this.addDrawableChild(new ButtonWidget(this.width/2 - 90, lastY, 180, 14, new LiteralText((f.getName() + " | " + ((float)f.length()/1024f/1024f) + " " + translation("mcvmcomputers.vhd_setup.mb_used"))), this::selectOld));
				this.addDrawableChild(new ButtonWidget(this.width/2 + 92, lastY, 14, 14, new LiteralText("x"), (wdgt) -> removevhd(f.getName())));
				lastY += 16;
			}

			int menuWidth = textRenderer.getWidth(translation("mcvmcomputers.vhd_setup.menu"))+40;
			this.addDrawableChild(new ButtonWidget(this.width - (menuWidth+10), this.height - 30, menuWidth, 20, new LiteralText(translation("mcvmcomputers.vhd_setup.menu")), (wdgt) -> switchState(State.MENU)));
		}
	}

	private void extset(Ext ext){
		extension=ext;
		if (extension==Ext.vdi){
			AA.active=false;
			BB.active=true;
		}else if (extension==Ext.vmdk){
			BB.active=false;
			AA.active=true;
		}
	}

	private void switchState(State newState) {
		this.children().clear();
		currentState = newState;
		this.init();
	}

	private void selectOld(ButtonWidget wdgt) {
		String fileName = wdgt.getMessage().asString().split(" ")[0];
		PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
		pb.writeString(fileName);
		ClientPlayNetworking.send(PacketList.C2S_CHANGE_HDD, pb);
		minecraft.openScreen(null);
	}

	private void createNew(ButtonWidget wdgt) {
		if(!status.startsWith(COLOR_CHAR + "c")) {
			Long size = Long.parseLong(hddSize.getText())*1024L*1024L;
			int i = ClientMod.latestVHDNum;
			File vhd = new File(ClientMod.vhdDirectory, "vhd" + i + "."+extension);
			IMedium hdd = null;
			if(extension == Ext.vdi){
				hdd = ClientMod.vb.createMedium("vdi", vhd.getPath(), AccessMode.ReadWrite, DeviceType.HardDisk);
			}else if(extension == Ext.vmdk){
				hdd = ClientMod.vb.createMedium("vmdk", vhd.getPath(), AccessMode.ReadWrite, DeviceType.HardDisk);
			}
			IProgress pr = Objects.requireNonNull(hdd).createBaseStorage(size, List.of(MediumVariant.Standard));
			pr.waitForCompletion(-1);

			try {
				ClientMod.increaseVHDNum();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
			pb.writeString(vhd.getName());
			ClientPlayNetworking.send(PacketList.C2S_CHANGE_HDD, pb);
			minecraft.openScreen(null);
		}
	}

	private void removevhd(String name) {
		new File(ClientMod.vhdDirectory, name).delete();
		minecraft.openScreen(null);
	}

	private void hddSizeUpdate(String in) {
		if(NumberUtils.isDigits(in)) {
			long i;
			try {
				i = Long.parseLong(in);
			}catch(NumberFormatException e) {
				status = translation("mcvmcomputers.input_parser_error");
				return;
			}
			if(i > 0) {
				if(i*1024*1024 < 0) { //Buffer overflow
					status = translation("mcvmcomputers.input_too_much").replace("%s", ""+Long.MAX_VALUE/1024L/1024L);
					return;
				}

				if(i*1024*1024 >= ClientMod.vhdDirectory.getFreeSpace()) {
					status = translation("mcvmcomputers.vhd_setup.space");
				}else {
					status = translation("mcvmcomputers.vhd_setup.validspace");
				}
			}else {
				status = translation("mcvmcomputers.input_too_little").replace("%s", "1");
			}
		}else {
			status = translation("mcvmcomputers.input_nan");
		}
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		this.renderBackground(ms);
		if(currentState == State.CREATE_NEW) {
			this.textRenderer.draw(ms, status, this.width / 2f - 150, this.height / 2f + 13, -1);
			this.textRenderer.draw(ms, translation("mcvmcomputers.vhd_setup.vhdsize"), this.width / 2f - 150, this.height / 2f-20, -1);
			this.hddSize.render(ms, mouseX, mouseY, delta);
		}else if(currentState == State.MENU) {
			String s = translation("mcvmcomputers.vhd_setup.setupnewvhd");
			this.textRenderer.draw(ms, s, this.width / 2f - this.textRenderer.getWidth(s) / 2f, this.height / 2f - 30, -1);
		}
		super.render(ms, mouseX, mouseY, delta);
	}

}

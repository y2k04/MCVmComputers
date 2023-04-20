package mcvmcomputers.client.gui.setup.pages;

import java.io.File;
import java.util.Objects;

import org.apache.commons.lang3.SystemUtils;

import mcvmcomputers.client.gui.setup.GuiSetup;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class SetupPageVboxDirectory extends SetupPage{
	private TextFieldWidget vboxDirectory;
	private ButtonWidget next;
	private String vboxStatus;
	
	public SetupPageVboxDirectory(GuiSetup setupGui, TextRenderer textRender) {
		super(setupGui, textRender);
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		this.textRender.draw(ms, setupGui.translation("mcvmcomputers.setup.vbox_dir"), setupGui.width / 2f - 160, setupGui.height / 2f - 20, -1);
		this.textRender.draw(ms, vboxStatus, setupGui.width / 2f - 160, setupGui.height / 2f + 13, -1);
		this.textRender.draw(ms, setupGui.translation("mcvmcomputers.setup.dontchange0"), setupGui.width / 2f - 160, 60, -1);
		this.textRender.draw(ms, setupGui.translation("mcvmcomputers.setup.dontchange1"), setupGui.width / 2f - 160, 70, -1);
		this.vboxDirectory.render(ms, mouseX, mouseY, delta);
	}
	
	private void next(ButtonWidget bw) {
		if(checkDirectory(vboxDirectory.getText())) {
			this.setupGui.virtualBoxDirectory = vboxDirectory.getText();
			this.setupGui.nextPage();
		}
	}
	
	private boolean checkDirectory(String s) {
		if(s.isEmpty()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_empty");
			next.active = false;
			return false;
		}
		File vboxDir = new File(s);
		if(!vboxDir.exists()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_empty");
			next.active = false;
			return false;
		}else if(vboxDir.isFile()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notfound");
			next.active = false;
			return false;
		}else {
			if(SystemUtils.IS_OS_WINDOWS) {
				if(!new File(vboxDir, "vboxmanage.exe").exists() || !new File(vboxDir, "vboxwebsrv.exe").exists()) {
					vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notvbox");
					next.active = false;
					return false;
				}
			}else if(SystemUtils.IS_OS_MAC) {
				if(!new File(vboxDir, "VBoxManage").exists() || !new File(vboxDir, "vboxwebsrv").exists()) {
					vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notvbox");
					next.active = false;
					return false;
				}
			}
		}
		vboxStatus = setupGui.translation("mcvmcomputers.input_dir_yesvbox");
		next.active = true;
		return true;
	}

	@Override
	public void init() {
		int nextButtonW = textRender.getWidth(setupGui.translation("mcvmcomputers.setup.nextButton"))+40;
		next = new ButtonWidget(setupGui.width/2 - (nextButtonW/2), setupGui.height - 40, nextButtonW, 20, new LiteralText(setupGui.translation("mcvmcomputers.setup.nextButton")), this::next);
		String dirText = setupGui.virtualBoxDirectory;
		if(dirText == null) {
			if(SystemUtils.IS_OS_WINDOWS)
				dirText = "C:\\Program Files\\Oracle\\VirtualBox";
				else if(SystemUtils.IS_OS_MAC)
					dirText = "/Applications/VirtualBox.app/Contents/MacOS";
		}

		checkDirectory(Objects.requireNonNull(dirText));
		vboxDirectory = new TextFieldWidget(this.textRender, setupGui.width/2 - 160, setupGui.height/2 - 10, 320, 20, new LiteralText(""));
		vboxDirectory.setMaxLength(35565);
		vboxDirectory.setText(dirText);
		vboxDirectory.setChangedListener(this::checkDirectory);
		setupGui.addDrawableChild(vboxDirectory);
		setupGui.addButton(next);
	}

}

package info.jbcs.minecraft.vending;

import info.jbcs.minecraft.utilities.DummyContainer;
import info.jbcs.minecraft.utilities.GuiHandler;
import info.jbcs.minecraft.utilities.ItemMetaBlock;
import info.jbcs.minecraft.utilities.packets.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid=Vending.MOD_ID, name=Vending.MOD_NAME, version=Vending.VERSION) // dependencies = "required-after:autoutils"
public class Vending {
	public static final String MOD_ID = "vending";
	public static final String MOD_NAME = "vending";
	public static final String VERSION = "1.2.0b";

	public static FMLEventChannel Channel;

	public static Block blockVendingMachine;
	public static Block blockAdvancedVendingMachine;
	public static Item itemWrench;
	
	public static GuiHandler guiVending;
	public static GuiHandler guiWrench;

	public static CreativeTabs	tabVending;
	
	static Configuration config;

	static Block[] supports={
			Block.stone,
			Block.cobblestone,
			Block.stoneBrick,
			Block.planks,
			Block.workbench,
			Block.gravel,
			Block.music,
			Block.sandStone,
			Block.blockGold,
			Block.blockIron,
			Block.brick,
			Block.cobblestoneMossy,
			Block.obsidian,
			Block.blockDiamond,
			Block.blockEmerald,
			Block.blockLapis,
	};
	static Object[] reagents={
			Block.stone,
			Block.cobblestone,
			Block.stoneBrick,
			Block.planks,
			Block.workbench,
			Block.gravel,
			Block.music,
			Block.sandStone,
			Item.ingotGold,
			Item.ingotIron,
			Block.brick,
			Block.cobblestoneMossy,
			Block.obsidian,
			Item.diamond,
			Item.emerald,
			Block.blockLapis,
	};

	@Instance("Vending")
	public static Vending instance;

	@SidedProxy(clientSide = "info.jbcs.minecraft.vending.ProxyClient", serverSide = "info.jbcs.minecraft.vending.Proxy")
	public static Proxy proxy;

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		proxy.preInit();
	}
	
	int getBlockId(String name,int id){
		return Vending.config.getBlock(name, id).getInt(id);
	}

	int getItemId(String name,int id){
		return Vending.config.getItem(name, id).getInt(id);
	}

	@Init
	public void init(FMLInitializationEvent event) {
		proxy.init();


		if(config.get("general", "use custom creative tab", true, "Add a new tab to creative mode and put all vending blocks there.").getBoolean(true)){
			tabVending = new CreativeTabs("tabVending") {
				@Override
				public ItemStack getIconItemStack() {
					return new ItemStack(blockVendingMachine, 1, 4);
				}
			};
			
			LanguageRegistry.instance().addStringLocalization("itemGroup.tabVending", "en_US", "Vending");
		} else{
			tabVending = CreativeTabs.tabDecorations;
		}
		
		blockVendingMachine = new BlockVendingMachine(getBlockId("vendingMachine",2391),supports,false).setUnlocalizedName("vendingMachine");
		LanguageRegistry.addName(blockVendingMachine, "Vending Block");
		GameRegistry.registerBlock(blockVendingMachine, ItemMetaBlock.class, "vendingMachine");

		blockAdvancedVendingMachine = new BlockVendingMachine(getBlockId("vendingMachineAdvanced",2392),supports,true).setUnlocalizedName("vendingMachineAdvanced");
		LanguageRegistry.addName(blockAdvancedVendingMachine, "Advanced Vending Block");
		GameRegistry.registerBlock(blockAdvancedVendingMachine, ItemMetaBlock.class, "vendingMachineAdvanced");

		itemWrench = new Item(getItemId("wrench",7820)).setUnlocalizedName("Vending:wrench").setCreativeTab(tabVending).setTextureName("Vending:wrench");
		LanguageRegistry.addName(itemWrench, "Vending Block Wrench");

		
        GameRegistry.registerTileEntity(TileEntityVendingMachine.class, "containerVendingMachine");

		for(int i=0;i<supports.length;i++){
			CraftingManager.getInstance().addRecipe(new ItemStack(blockVendingMachine,1,i),
					new Object[] { "XXX", "XGX", "*R*",
					'X', Block.glass,
					'G', Item.ingotGold,
					'R', Item.redstone,
					'*', reagents[i],
				});
			
			CraftingManager.getInstance().addRecipe(new ItemStack(blockAdvancedVendingMachine,1,i),
					new Object[] { "XXX", "XGX", "*R*",
					'X', Block.glass,
					'G', Item.ingotGold,
					'R', Item.redstoneRepeater,
					'*', reagents[i],
				});
		}
		
		guiVending=new GuiHandler("vending"){
			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		        if(! (tileEntity instanceof TileEntityVendingMachine))
		        	return null;
		        
		        TileEntityVendingMachine e=(TileEntityVendingMachine) tileEntity;
		        
		        if(e.advanced)
		        	return new ContainerAdvancedVendingMachine(player.inventory, e);
		        
		        return new ContainerVendingMachine(player.inventory, e);
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		        if(! (tileEntity instanceof TileEntityVendingMachine))
		        	return null;
		        
		        TileEntityVendingMachine e=(TileEntityVendingMachine) tileEntity;
		        
		        if(e.advanced)
                    return new GuiAdvancedVendingMachine(player.inventory, e);

                return new GuiVendingMachine(player.inventory, e);
			}
		};
		
		guiWrench=new GuiHandler("wrench"){
			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		        return new DummyContainer();
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
               
                return new GuiWrenchVendingMachine(world,x,y,z,player);
			}
		};

		GuiHandler.register(this);
		
		Packets.advancedMachine.create();
		Packets.wrench.create();
		PacketHandler.register(this);
	}

	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
	}
}



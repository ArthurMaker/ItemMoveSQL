package itemmovesql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InvConstructUtils {

	// ench@level|ench@level
	public static  String EnchantmentsToString(ItemStack i)
	{
		String e ="";
		for (Enchantment ent : i.getEnchantments().keySet())
		{
			e+= "|"+ent.getName()+":"+i.getEnchantments().get(ent).intValue();
		}
		if (e!="") {e = e.substring(1);}
		return e;
	}
	
	//lore|lore
	public static String LoreToString(ItemStack i)
	{
		String l ="";
		for (String lore : i.getItemMeta().getLore())
		{
			l += "|"+lore;
		}
		if (l!="") {l = l.substring(1);}
		return l;
	}
	
	
	public static ItemStack ResultSetToItemStack(ResultSet rs)
	{
		ItemStack itemtogive = new ItemStack(1); //fake itemstack
		try {
			itemtogive.setTypeId(rs.getInt(2)); //set type
			itemtogive.setDurability((short)(rs.getInt(3))); //set durability
			itemtogive.setAmount(rs.getInt(4)); //set amount
			if (!rs.getString(5).equals("none")) //apply enchants if needed
			{
				itemtogive.addEnchantments(StringToEnchantments(rs.getString(5)));
			}
			if (!rs.getString(6).equals("none")) //apply lore if needed
			{
				ItemMeta im = itemtogive.getItemMeta();
				im.setLore(StringToLore(rs.getString(6)));
				itemtogive.setItemMeta(im);
			}
			if (!rs.getString(7).equals("none"))
			{
				ItemMeta im = itemtogive.getItemMeta();
				im.setDisplayName(rs.getString(7));
				itemtogive.setItemMeta(im);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return itemtogive;
	}
	
	private static Map<Enchantment,Integer> StringToEnchantments(String estring)
	{
		Map<Enchantment,Integer> enchants = new HashMap<Enchantment,Integer>();
		String[] ea = estring.split("[|]");
		for (int i=0; i<ea.length; i++)
		{
			String[] einfo = ea[i].split("[:]");
			enchants.put(Enchantment.getByName(einfo[0]), Integer.valueOf(einfo[1]));
		}
		return enchants;
		
	}
	
	private static List<String> StringToLore(String lstring)
	{
		List<String> lore = new ArrayList<String>();
		for (String l : lstring.split("[|]"))
		{
			lore.add(l);
		}
		return lore;
	}
}

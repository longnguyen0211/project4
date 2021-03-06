package vn.aptech.project4.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.aptech.project4.entity.Ingredient;
import vn.aptech.project4.entity.Inventory;
import vn.aptech.project4.repository.IngredientRepository;
import vn.aptech.project4.repository.InventoryRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/ingredient")
public class IngredientController {
	private IngredientRepository ingredientRepository;
	private InventoryRepository inventoryRepository;
	private int lowStock;
	@Autowired
	public IngredientController(IngredientRepository ingredientRepository,InventoryRepository inventoryRepository) {
		this.ingredientRepository = ingredientRepository;
		this.inventoryRepository = inventoryRepository;
	}
	public void getInventoryNotification(Model theModel){
		lowStock=0;
		List<Inventory> theList = inventoryRepository.findAll();
		for(Inventory temp:theList){
			if(temp.getQuantity()<temp.getSafetyStock()){
				lowStock+=1;
			}
		}
		if(lowStock==1){
			theModel.addAttribute("lowStockMsg",lowStock+" Item Inventory Low");
		}else if (lowStock>1){
			theModel.addAttribute("lowStockMsg",lowStock+" Items Inventory Low");
		}
		theModel.addAttribute("lowInventory", lowStock);
	}
	@GetMapping("/list")
	public String showIngredients(Model theModel,@ModelAttribute(value = "successMsg")String message) {
		if(message.isEmpty()){
			message=null;
		}
		theModel.addAttribute("successMsg",message);
		getInventoryNotification(theModel);
		theModel.addAttribute("ingredients", ingredientRepository.findAll() );
		return "list-ingredients";
	}

	@GetMapping("/create")
	public String addIngredient(Model theModel) {
		getInventoryNotification(theModel);
		theModel.addAttribute("ingredient", new Ingredient());
		return "form-ingredient";
	}

	@PostMapping("/create")
	public String addIngredient(@ModelAttribute(value = "ingredient") Ingredient ingredient, ModelMap theModelMap, RedirectAttributes redirectAttributes) {
		theModelMap.addAttribute("ingredient", ingredient);
		ingredientRepository.save(ingredient);
		Inventory inventory = new Inventory();
		inventory.setIngredient(ingredient);
		inventory.setQuantity(0);
		inventory.setPrice(0);
        inventory.setStatus(1);
        inventory.setRatio(1);
		inventory.setUnit("N/A");
		inventory.setVendorName("N/A");
		inventoryRepository.save(inventory);
		redirectAttributes.addFlashAttribute("successMsg","Add New Ingredient");
		return "redirect:/admin/ingredient/list";
	}

	@GetMapping("/update/{id}")
	public String editIngredient(@PathVariable(value = "id") int theId, Model theModel) {
		getInventoryNotification(theModel);
		Optional<Ingredient> theIngredient1 = ingredientRepository.findById(theId);
		if (theIngredient1.isPresent()) {
			Ingredient theIngredient = theIngredient1.get();
			theModel.addAttribute("ingredient", theIngredient);
		}
		return "update-ingredient";
	}

	@GetMapping("/delete/{id}")
	public String deleteIngredient(@PathVariable(value = "id") int theId, Model theModel) {
		Optional<Ingredient> theIngredient1 = ingredientRepository.findById(theId);
		try {
			if (theIngredient1.isPresent()) {
				Ingredient theIngredient = theIngredient1.get();
				ingredientRepository.delete(theIngredient);
				theModel.addAttribute("message", "Cannot Delete, Please Check Inventory!");
			}
		} catch (Exception e) {
			theModel.addAttribute("message", "Cannot Delete, Please Check Inventory!");
		}
		
		return "redirect:/admin/ingredient/list";
	}
	@PostMapping("/update")
	public String updateIngredient(@ModelAttribute(value = "ingredient") Ingredient ingredient, ModelMap theModelMap, RedirectAttributes redirectAttributes) {
		theModelMap.addAttribute("ingredient", ingredient);
		ingredientRepository.save(ingredient);
		redirectAttributes.addFlashAttribute("successMsg","Update Ingredient");
		return "redirect:/admin/ingredient/list";
	}
}

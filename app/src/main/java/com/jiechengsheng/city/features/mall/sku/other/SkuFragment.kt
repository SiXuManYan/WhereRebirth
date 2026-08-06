package com.jiechengsheng.city.features.mall.sku.other

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.jiechengsheng.city.R
import com.jiechengsheng.city.api.response.mall.MallAttribute
import com.jiechengsheng.city.base.mvp.BaseBottomSheetDialogFragment
import com.jiechengsheng.city.base.mvp.FixedHeightBottomSheetDialog
import com.jiechengsheng.city.features.mall.sku.bean.Sku
import com.jiechengsheng.city.features.mall.sku.bean.SkuAttribute
import com.jiechengsheng.city.features.mall.sku.view.OnSkuListener
import com.jiechengsheng.city.utils.BusinessUtils
import com.jiechengsheng.city.utils.GlideUtil
import com.jiechengsheng.city.databinding.DialogProductSkuBinding
/**
 * Created by Wangsw  2022/7/26 15:46.
 *
 */
class SkuFragment : BaseBottomSheetDialogFragment<SkuPresenter>(), SkuView {

    private var _binding: DialogProductSkuBinding? = null
    private val binding get() = _binding!!


    private var product: Product? = null
    private var skuListData: List<Sku>? = null
    var callback: Callback? = null
    var selectedSku: Sku? = null

    var allAttrList = ArrayList<MallAttribute>()

    override fun getLayoutId() = R.layout.dialog_product_sku


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        FixedHeightBottomSheetDialog(requireContext(), theme, ScreenUtils.getScreenHeight() * 88 / 100)

    override fun initView(parent: View) {
        _binding = DialogProductSkuBinding.bind(requireNotNull(view))
        // 不可编辑
        binding.numberValueEt.isFocusable = false
        binding.numberValueEt.isFocusableInTouchMode = false

    }

    override fun initData() {
        presenter = SkuPresenter(this)
        updateSkuData2()
//        updateQuantityOperator(1)

        showLastSku(selectedSku)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    

    override fun bindListener() {
        binding.backIv.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.numberCutIv.setOnClickListener {
            val quantity = binding.numberValueEt.getText().toString()
            if (TextUtils.isEmpty(quantity)) {
                return@setOnClickListener
            }
            val quantityInt = quantity.toInt()
            if (quantityInt > 1) {
                val newQuantity = (quantityInt - 1).toString()
                binding.numberValueEt.setText(newQuantity)
                binding.numberValueEt.setSelection(newQuantity.length)
                updateQuantityOperator(quantityInt - 1)
            }
        }

        binding.numberAddIv.setOnClickListener {
            val quantity = binding.numberValueEt.text.toString()

            if (TextUtils.isEmpty(quantity) || selectedSku == null) {
                return@setOnClickListener
            }
            val quantityInt = quantity.toInt()
            if (quantityInt < selectedSku!!.stockQuantity) {
                val newQuantity = (quantityInt + 1).toString()
                binding.numberValueEt.setText(newQuantity)
                binding.numberValueEt.setSelection(newQuantity.length)
                updateQuantityOperator(quantityInt + 1)
            }
        }

        binding.numberValueEt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE || selectedSku == null) {
                return@setOnEditorActionListener false
            }
            val quantity = binding.numberValueEt.getText().toString()
            if (TextUtils.isEmpty(quantity)) {
                return@setOnEditorActionListener false
            }
            val quantityInt = quantity.toInt()
            when {
                quantityInt <= 1 -> {
                    binding.numberValueEt.setText("1")
                    binding.numberValueEt.setSelection(1)
                    updateQuantityOperator(1)
                }
                quantityInt >= selectedSku!!.stockQuantity -> {
                    val newQuantity = selectedSku!!.stockQuantity.toString()
                    binding.numberValueEt.setText(newQuantity)
                    binding.numberValueEt.setSelection(newQuantity.length)
                    updateQuantityOperator(selectedSku!!.stockQuantity)
                }
                else -> {
                    binding.numberValueEt.setSelection(quantity.length)
                    updateQuantityOperator(quantityInt)
                }
            }
            false

        }

        binding.scrollSkuList.setListener(object : OnSkuListener {
            override fun onUnselected(unselectedAttribute: SkuAttribute?) {
                selectedSku = null

                product?.let {
                    GlideUtil.load(context, it.main_image, binding.ivSkuLogo)
                    binding.tvSkuQuantity.text = StringUtils.getString(R.string.stock_format, it.stock)
                }


                val firstUnselectedAttributeName = binding.scrollSkuList.firstUnelectedAttributeName
                binding.tvSkuInfo.text = "请选择：$firstUnselectedAttributeName"
//                binding.confirmTv.isEnabled = false


                val quantity = binding.numberValueEt.text.toString()
                if (!TextUtils.isEmpty(quantity)) {
                    updateQuantityOperator(Integer.valueOf(quantity))
                } else {
                    updateQuantityOperator(0)
                }
            }

            override fun onSelect(selectAttribute: SkuAttribute?) {
                val firstUnselectedAttributeName = binding.scrollSkuList.getFirstUnelectedAttributeName()
                binding.tvSkuInfo.setText("请选择：$firstUnselectedAttributeName")
            }

            override fun onSkuSelected(sku: Sku?) {
                selectedSku = sku
                if (selectedSku == null) {
                    return
                }

                GlideUtil.load(context, selectedSku!!.mainImage, binding.ivSkuLogo)
                val attributeList = selectedSku!!.attributes
                val builder = StringBuilder()
                for (i in attributeList.indices) {
                    if (i != 0) {
                        builder.append("　")
                    }
                    val attribute = attributeList[i]
                    builder.append("\"" + attribute.value + "\"")
                }
                binding.tvSkuInfo.text = "已选：$builder"

                binding.tvSkuQuantity.text = StringUtils.getString(R.string.stock_format, selectedSku!!.stockQuantity)

                val sellingPrice = selectedSku!!.sellingPrice
                val originPrice = selectedSku!!.originPrice

                BusinessUtils.setNowPriceAndOldPrice(sellingPrice, originPrice, binding.tvSkuSellingPrice, binding.originalPriceTv)

//                binding.confirmTv.isEnabled = true
                val quantity = binding.numberValueEt.text.toString()
                if (!TextUtils.isEmpty(quantity)) {
                    updateQuantityOperator(Integer.valueOf(quantity))
                } else {
                    updateQuantityOperator(0)
                }
            }

        })

        binding.confirmTv.setOnClickListener {

            if (selectedSku == null) {
                ToastUtils.showShort(R.string.please_selected)
                return@setOnClickListener
            }

            val size = selectedSku!!.attributes.size
            if (size != allAttrList.size) {
                ToastUtils.showShort(R.string.please_selected)
                return@setOnClickListener
            }

            if (selectedSku!!.stockQuantity <= 0) {
                ToastUtils.showShort(R.string.inventory_shortage_select)
                return@setOnClickListener
            }


            val quantity = binding.numberValueEt.text.toString()

            if (TextUtils.isEmpty(quantity)) {
                return@setOnClickListener
            }
            val quantityInt = quantity.toInt()
            if (quantityInt > 0 && quantityInt <= selectedSku!!.stockQuantity) {
                callback?.onAdded(selectedSku, quantityInt)
                dismissAllowingStateLoss()
            } else {
                ToastUtils.showShort("The quantity is out of stock, please revise")
            }
        }
    }


    fun setData(product: Product, attributeList: ArrayList<MallAttribute>) {
        this.product = product
        skuListData = product.skus
        allAttrList.addAll(attributeList)
    }

    private fun showLastSku(lastSku: Sku?) {
        if (product == null || lastSku == null) {
            return
        }
        if (lastSku.stockQuantity > 0) {
            selectedSku = lastSku
            // 选中第一个sku
            binding.scrollSkuList.selectedSku = selectedSku
            GlideUtil.load(context, selectedSku?.mainImage, binding.ivSkuLogo)

            val sellingPrice = selectedSku!!.sellingPrice
            val originPrice = selectedSku!!.originPrice

            BusinessUtils.setNowPriceAndOldPrice(sellingPrice, originPrice, binding.tvSkuSellingPrice, binding.originalPriceTv)


            binding.tvSkuQuantity.text = StringUtils.getString(R.string.stock_format, selectedSku!!.stockQuantity)

//            binding.confirmTv.isEnabled = selectedSku!!.stockQuantity > 0
            val attributeList = selectedSku!!.attributes
            val builder = StringBuilder()
            for (i in attributeList.indices) {
                if (i != 0) {
                    builder.append("　")
                }
                val attribute = attributeList[i]
                builder.append("\"" + attribute.value + "\"")
            }
            binding.tvSkuInfo.text = "已选：$builder"

        }


    }


    private fun updateSkuData2() {
        if (product == null) {
            return
        }

        if (product!!.skus.isEmpty()) {
            return
        }
        GlideUtil.load(context, product!!.main_image, binding.ivSkuLogo)

        val sellingPrice = product!!.price
        val originalCost = product!!.original_cost

        BusinessUtils.setNowPriceAndOldPrice(sellingPrice, originalCost, binding.tvSkuSellingPrice, binding.originalPriceTv)

        binding.tvSkuQuantity.text = StringUtils.getString(R.string.stock)

//        binding.confirmTv.isEnabled = false

        if (!skuListData.isNullOrEmpty() && !skuListData!![0].attributes.isNullOrEmpty()) {
            binding.tvSkuInfo.text = "请选择：" + skuListData!![0].attributes[0].key
        }

        // 处理sku 属性为空
        if (allAttrList.isEmpty() && product!!.skus.isNotEmpty()) {
            selectedSku = product!!.skus[0]
            updateQuantityOperator(0)

        }
        binding.scrollSkuList.setSkuList(product!!.skus)


    }


    public fun updateQuantityOperator(newQuantity: Int) {
        if (selectedSku == null) {
            binding.numberCutIv.isEnabled = false
            binding.numberAddIv.isEnabled = false
            binding.numberValueEt.isEnabled = false

            binding.numberCutIv.setImageResource(R.mipmap.ic_cut_blue_transparent)
            binding.numberAddIv.setImageResource(R.mipmap.ic_add_blue_transparent)


        } else {
            when {
                newQuantity <= 1 -> {
                    binding.numberCutIv.isEnabled = false
                    binding.numberAddIv.isEnabled = true

                    binding.numberCutIv.setImageResource(R.mipmap.ic_cut_blue_transparent)
                    binding.numberAddIv.setImageResource(R.mipmap.ic_add_blue)
                }
                newQuantity >= selectedSku!!.stockQuantity -> {
                    binding.numberCutIv.isEnabled = true
                    binding.numberAddIv.isEnabled = false

                    binding.numberCutIv.setImageResource(R.mipmap.ic_cut_blue)
                    binding.numberAddIv.setImageResource(R.mipmap.ic_add_blue_transparent)
                }
                else -> {
                    binding.numberCutIv.isEnabled = true
                    binding.numberAddIv.isEnabled = true

                    binding.numberCutIv.setImageResource(R.mipmap.ic_cut_blue)
                    binding.numberAddIv.setImageResource(R.mipmap.ic_add_blue)
                }
            }
            binding.numberValueEt.isEnabled = true
        }
    }


    interface Callback {
        fun onAdded(sku: Sku?, quantity: Int)
    }

}
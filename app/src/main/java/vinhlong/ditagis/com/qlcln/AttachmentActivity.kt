package vinhlong.ditagis.com.qlcln

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import vinhlong.ditagis.com.qlcln.adapter.ViewAttachmentAsync
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import kotlinx.android.synthetic.main.activity_attachment.*
import org.apache.commons.io.IOUtils
import vinhlong.ditagis.com.qlcln.adapter.FeatureViewMoreInfoAttachmentsAdapter
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.utities.DAlertDialog
import vinhlong.ditagis.com.qlcln.utities.DBitmap


class AttachmentActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment)

        try {
            shimmer_view_container.startShimmerAnimation()
            mApplication = application as DApplication
            val lstViewAttachment = lstView_alertdialog_attachments

            val attachmentsAdapter = FeatureViewMoreInfoAttachmentsAdapter(this, mutableListOf())
            lstViewAttachment.adapter = attachmentsAdapter


            val viewAttachmentAsync = ViewAttachmentAsync(this, container_attachment, (mApplication.selectedFeature as ArcGISFeature?)!!, object : ViewAttachmentAsync.AsyncResponse {
                override fun processFinish(attachment: Attachment?) {
                    if (attachment == null){
                        DAlertDialog().show(this@AttachmentActivity, "Thông báo","Không có ảnh đính kèm!")
                        shimmer_view_container.stopShimmerAnimation()
                        shimmer_view_container.visibility = View.GONE
                        return
                    }
                    var item = FeatureViewMoreInfoAttachmentsAdapter.Item()

                    item.attachment = attachment
                    val inputStreamListenableFuture = item.attachment?.fetchDataAsync()
                    inputStreamListenableFuture?.addDoneListener {
                        try {
                            val inputStream = inputStreamListenableFuture.get()
                            val img = IOUtils.toByteArray(inputStream)
                            val bmp = DBitmap().getBitmap(img)
                            item.bitmap = bmp
                            attachmentsAdapter.add(item)
                            attachmentsAdapter.notifyDataSetChanged()
                            shimmer_view_container.stopShimmerAnimation()
                            shimmer_view_container.visibility = View.GONE
                        } catch (e: Exception) {
                            DAlertDialog().show(this@AttachmentActivity, e)
                        }
                    }

                }

            })
            viewAttachmentAsync.execute()
            lstViewAttachment.setOnItemClickListener { parent, view, position, id ->
                run {
                    val item = attachmentsAdapter.getItem(position)
                    if (item != null) {

                        mApplication.selectedAttachment = item.attachment
                        mApplication.selectedBitmap = item.bitmap
                        val intent = Intent(this@AttachmentActivity, HandlerAttachmentActivity::class.java)
                        this@AttachmentActivity.startActivity(intent)

                    } else {
                        Toast.makeText(this@AttachmentActivity, "Không có ảnh!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
           DAlertDialog().show(this, e)
        }
    }

}

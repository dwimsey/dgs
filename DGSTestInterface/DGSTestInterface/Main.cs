using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

using DGSTestInterface.DGS;

namespace DGSTestInterface
{
	public partial class Main : Form
	{
		public Main()
		{
			InitializeComponent();
			cb_ImageFormat.SelectedItem = "PNG";
		}

		private string getImageTypeExtension()
		{
			
			switch(cb_ImageFormat.SelectedItem.ToString().ToLower()) {
				case "png":
					return (".png");
				case "jpg":
					return (".jpg");
				case "tiff":
					return (".tif");
				case "pdf":
					return (".pdf");
				default:
					throw new Exception("Unexpected image format specified.");
			}
		}

		private void btn_InputFile_Click(object sender, EventArgs e)
		{
			OpenFileDialog fd = new OpenFileDialog();
			fd.CheckFileExists = true;
			fd.CheckPathExists = true;
			if(fd.ShowDialog() == DialogResult.OK) {
				tb_InputFile.Text = fd.FileName;
				UpdateOutputFilename();
			}
		}

		private void btn_ProcessImages_Click(object sender, EventArgs e)
		{
			tb_ProcessingLog.Text = "Begining processing request by reading in SVG: " + tb_InputFile.Text + "\r\n";
			tb_ProcessingLog.Update();
			System.IO.FileStream fs = System.IO.File.Open(tb_InputFile.Text, System.IO.FileMode.Open, System.IO.FileAccess.Read, System.IO.FileShare.Read);
			byte[] inStr = new byte[fs.Length];
			fs.Read(inStr, 0, (int)fs.Length);
            fs.Close();

			ImageProcesserService dgs = new ImageProcesserService();
			dgsRequestInfo requestInfo = new dgsRequestInfo();
			requestInfo.continueOnError = false;
			requestInfo.files = new dgsFileInfo[1];
			requestInfo.files[0] = new dgsFileInfo();
			requestInfo.files[0].mimeType = "image/svg+xml";
			requestInfo.files[0].name = "test.svg";
			requestInfo.files[0].width = 0;
			requestInfo.files[0].height = 0;
			requestInfo.files[0].data = inStr;
			String outputMimeType = "image/png";
			switch(cb_ImageFormat.SelectedItem.ToString().ToLower()) {
				default:
					outputMimeType = "image/png";
					MessageBox.Show(this, "The image output type selected is unknown, PNG will be used instead.");
					break;
				case "png":
					outputMimeType = "image/png";
					break;
				case "jpg":
					outputMimeType = "image/jpeg";
					break;
				case "tiff":
					outputMimeType = "image/tiff";
					break;
				case "pdf":
					outputMimeType = "application/pdf";
					break;
			}
			requestInfo.instructionsXML = "<commands><load filename=\"test.svg\" buffer=\"main\" mimeType=\"image/svg+xml\" /><save snapshotTime=\"1.0\" filename=\"test.gif\" buffer=\"main\" mimeType=\"" + outputMimeType + "\" /></commands>";

			DGS.dgsResponseInfo responseInfo = null;
			tb_ProcessingLog.Text += "Issuing SOAP request ...\r\n";
			tb_ProcessingLog.Update();
			try {
				responseInfo = dgs.ProcessImage(requestInfo);
				tb_ProcessingLog.Text += "SOAP request completed.\r\n";
				tb_ProcessingLog.Update();
			} catch(Exception ex) {
				tb_ProcessingLog.Text += "An exception occured processing the SOAP request: " + ex.Message.ToString() + "\r\n";
				tb_ProcessingLog.Update();
				return;
			}

			tb_ProcessingLog.Text += "SOAP processing log is as follows:\r\n";
			tb_ProcessingLog.Update();
			foreach(string line in responseInfo.processingLog) {
				tb_ProcessingLog.Text += line + "\r\n";
			}

            if (responseInfo.resultFiles != null)
            {
                if (responseInfo.resultFiles.Length > 0)
                {
                    fs = System.IO.File.OpenWrite(tb_OutputFile.Text);
                    fs.Write(responseInfo.resultFiles[0].data, 0, responseInfo.resultFiles[0].data.Length);
                    fs.Close();
                }
                else
                {
                    tb_ProcessingLog.Text += "0 file results were returned.\r\n";
                    tb_ProcessingLog.Update();
                }
            }
            else
            {
                tb_ProcessingLog.Text += "No file results were returned.\r\n";
                tb_ProcessingLog.Update();
            }
		}

		private void cb_ImageFormat_SelectedIndexChanged(object sender, EventArgs e)
		{
			if(cb_ImageFormat.SelectedItem.ToString() == "") {
				return;
			}
			UpdateOutputFilename();
		}

		private void UpdateOutputFilename()
		{
			int offset;
			string of = null;
			if(tb_InputFile.Text != null && tb_InputFile.Text.Length > 0) {
				offset = tb_InputFile.Text.LastIndexOf(".");
				if(offset > -1) {
					of = tb_InputFile.Text.Substring(0, offset);
				} else {
					of = tb_InputFile.Text;
				}
			}
			offset = tb_OutputFile.Text.LastIndexOf(".");
			if(tb_OutputFile.Text != null && tb_OutputFile.Text.Length > 0) {
				if(offset > -1) {
					of = tb_OutputFile.Text.Substring(0, offset);
				} else {
					of = tb_OutputFile.Text;
				}
			}

			if(of != null && of.Length > 0) {
				of += getImageTypeExtension();
				tb_OutputFile.Text = of;
			}
		}
	}
}

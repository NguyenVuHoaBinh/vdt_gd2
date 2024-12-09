import React, { useRef, useState, useCallback, useEffect } from 'react';
import { useReactMediaRecorder } from 'react-media-recorder';
import { Button as ChatButton } from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { materialDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { FaMicrophone } from 'react-icons/fa';

const ChatBoxMydio = ({ model, modelType, connected, chatLog, setChatLog }) => {
  const sessionId = useRef(`session_${Date.now()}`).current;
  console.log('ChatBox component rendered with props:', { model, modelType, sessionId, connected, chatLog });
  const systemRole = `
  
  `;

  const [inputValue, setInputValue] = useState('');
  const [billInfo, setBillInfo] = useState(null);
  const [productInfo, setProductInfo] = useState(null);
  const [productAdjust,setProductAdjust] = useState(null);
  const [businessInfo,setBusinessInfo] = useState(null);
  const [inactiveInfo,setInactiveInfo] = useState(null);
  const [debtInfo,setDebtInfo] = useState(null);
  const [restockAlertInfo,setRestockAlertInfo] = useState(null);
  const [trendInfo,setTrendInfo] = useState(null);
  const chatRef = useRef(null);
  const silenceTimeoutRef = useRef(null);

  const { startRecording, stopRecording, mediaBlobUrl, status, clearBlobUrl } = useReactMediaRecorder({
    audio: true,
  });

  // Helper function to generate a table from the query result
  const generateTable = (data) => {
    console.log('Generating table with data:', data);
    if (!Array.isArray(data) || data.length === 0) return 'No data available.';

    const headers = Object.keys(data[0]);
    const headerRow = headers.map((header) => `<th>${header}</th>`).join('');
    const rows = data
      .map((row) => {
        const rowData = headers.map((header) => `<td>${row[header]}</td>`).join('');
        return `<tr>${rowData}</tr>`;
      })
      .join('');

    return `<table style="width: 100%; border-collapse: collapse; border: 1px solid black;">
      <thead><tr>${headerRow}</tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
  };



  const sendAudioToBackend = async (blobUrl) => {
    console.log('sendAudioToBackend called with blobUrl:', blobUrl);
    try {
      const response = await fetch(blobUrl);
      const audioBlob = await response.blob();
      const formData = new FormData();
      formData.append('file', audioBlob, 'audio.wav');

      const result = await fetch('https://localhost:8888/process-audio', {
        method: 'POST',
        body: formData,
      });

      console.log('Audio processing response status:', result.status);
      const data = await result.json();
      console.log('Audio processing result:', data);

      if (result.ok) {
        const transcribedText = data.transcription;
        console.log('Transcribed text:', transcribedText);
        setInputValue(transcribedText);
        // Automatically submit the transcribed text
        handleChatSubmit(transcribedText);
      } else {
        console.error('Error from backend:', data);
        setChatLog((prevChatLog) => [
          ...prevChatLog,
          { user: 'bot', text: 'Error processing audio' },
        ]);
      }
    } catch (error) {
      console.error('Error sending audio to backend:', error);
      setChatLog((prevChatLog) => [
        ...prevChatLog,
        { user: 'bot', text: 'Error sending audio to backend' },
      ]);
    } finally {
      clearBlobUrl();
    }
  };

  // Modified microphone handling logic for Case 1 and Case 2
  const handleMicrophoneClick = () => {
    console.log('handleMicrophoneClick called, status:', status);
    if (status === 'recording') {
      // Case 2: User presses the microphone again to stop recording and send
      stopRecording();
      console.log('Recording stopped by user.');
    } else {
      // Start recording
      startRecording();
      console.log('Recording started.');

      // Case 1: Stop recording automatically after 3 seconds of silence
      if (silenceTimeoutRef.current) {
        clearTimeout(silenceTimeoutRef.current);
        console.log('Cleared existing silence timeout.');
      }
      silenceTimeoutRef.current = setTimeout(() => {
        if (status === 'recording') {
          stopRecording();
          console.log('Recording stopped due to silence timeout.');
        }
      }, 3000);
    }
  };

  // UseEffect to automatically send audio once recording stops
  useEffect(() => {
    console.log('useEffect triggered for mediaBlobUrl:', mediaBlobUrl);
    if (mediaBlobUrl) {
      sendAudioToBackend(mediaBlobUrl);
    }
  }, [mediaBlobUrl]);

  return (
    <div style={styles.container}>
      <div style={styles.chatContainer}>
        <div style={styles.chatMessages}>
          {chatLog.map((entry, index) => (
            <div
              ref={chatRef}
              key={index}
              style={{
                display: 'flex',
                justifyContent: entry.user === 'user' ? 'flex-end' : 'flex-start',
                wordBreak: 'break-word', // Ensure text wraps properly
                overflowWrap: 'break-word',
              }}
            >
              <div
                style={{
                  maxWidth: '80%',
                  padding: '10px',
                  background: entry.user === 'user' ? '#ffffff' : '#ffffff',
                  borderRadius: '10px',
                  border: '2px solid #000000'
                }}
              >
                <ReactMarkdown
                  rehypePlugins={[rehypeRaw]}
                  remarkPlugins={[remarkGfm]}
                  components={{
                    code({ node, inline, className, children, ...props }) {
                      const match = /language-(\w+)/.exec(className || '');
                      return !inline && match ? (
                        <SyntaxHighlighter {...props} style={materialDark} language={match[1]} PreTag="div">
                          {String(children).replace(/\n$/, '')}
                        </SyntaxHighlighter>
                      ) : (
                        <code className={className} {...props}>
                          {children}
                        </code>
                      );
                    },
                  }}
                >
                  {entry.text}
                </ReactMarkdown>
              </div>
            </div>
          ))}
        </div>

        

        {mediaBlobUrl && (
          <div style={{ marginTop: '10px' }}>
            <audio src={mediaBlobUrl} controls />
            <ChatButton text="Send Audio" onClick={() => {
              console.log('Send Audio button clicked, mediaBlobUrl:', mediaBlobUrl);
              sendAudioToBackend(mediaBlobUrl);
            }} />
            <ChatButton text="Discard" onClick={() => {
              console.log('Discard button clicked, clearing blob URL.');
              clearBlobUrl();
            }} style={{ marginLeft: '10px' }} />
          </div>
        )}
      </div>
      <div style={styles.infoContainer}>
      {billInfo ? (
        <div style={styles.infoContent}>
          {/* Customer Information */}
          <h3>Thông tin khách hàng</h3>
          <p><strong>Họ và tên:</strong> {billInfo.customerName || 'N/A'}</p>
          <p><strong>Số điện thoại:</strong> {billInfo.phoneNumber || 'N/A'}</p>
          <p><strong>Cấp thành viên:</strong> {billInfo.membershipLevel || 'N/A'}</p>

          {/* Order Details */}
          <h3>Thông tin hóa đơn</h3>
          {billInfo.orderDetails && billInfo.orderDetails.length > 0 ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>STT</th>
                  <th style={styles.tableHeader}>Tên sản phẩm</th>
                  <th style={styles.tableHeader}>Số lượng</th>
                  <th style={styles.tableHeader}>Giá bán (VNĐ)</th>
                  <th style={styles.tableHeader}>Giảm giá sản phẩm (VNĐ)</th>
                  <th style={styles.tableHeader}>Thành tiền (VNĐ)</th>
                </tr>
              </thead>
              <tbody>
                {billInfo.orderDetails.map((item, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{index + 1}</td>
                    <td style={styles.tableCell}>{item.productName || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.quantity || 0}</td>
                    <td style={styles.tableCell}>{item.unitPrice || 0}</td>
                    <td style={styles.tableCell}>{item.discount || 0}</td>
                    <td style={styles.tableCell}>{item.totalPrice || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p></p>
          )}

          {/* Total Price */}
          <h3>Tổng hóa đơn</h3>
          <p><strong>Giảm giá thành viên ({billInfo.membershipLevel || 'N/A'} - {billInfo.membershipDiscount || 0}%):</strong> {billInfo.membershipDiscountAmount || 0} VNĐ</p>
          <p><strong>Giảm giá thêm:</strong> {billInfo.alternativeDiscount || 0} VNĐ</p>
          <p><strong>Tổng tiền thanh toán:</strong> {billInfo.totalAmount || 0} VNĐ</p>

          {/* Payment Method */}
          <h3>Phương thức thanh toán</h3>
          <p><strong>Phương thức thanh toán:</strong> {billInfo.paymentMethod || 'N/A'}</p>
          <p><strong>Hạn thanh toán:</strong> {billInfo.paymentDate || 'N/A'}</p>
          <p><strong>Thời gian lập hóa đơn:</strong> {billInfo.receiptDateTime || 'N/A'}</p>
        </div>
      ) : (
        <p></p>
      )}

      {productInfo ? (
        <div style={styles.infoContent}>
          {/* Product Information */}
          <h1>Thông tin sản phẩm</h1>
          {productInfo.importDetail && productInfo.importDetail.length > 0 ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>STT</th>
                  <th style={styles.tableHeader}>Tên sản phẩm</th>
                  <th style={styles.tableHeader}>Danh mục sản phẩm</th>
                  <th style={styles.tableHeader}>Đơn vị</th>
                  <th style={styles.tableHeader}>Xuất xứ</th>
                  <th style={styles.tableHeader}>Giá bán</th>
                  <th style={styles.tableHeader}>Kho hiện tại</th>
                  <th style={styles.tableHeader}>Giá nhập</th>
                  <th style={styles.tableHeader}>Phần trăm giảm giá</th>
                  <th style={styles.tableHeader}>Số lượng nhập thêm</th>
                </tr>
              </thead>
              <tbody>
                {productInfo.importDetail.map((item, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{index + 1}</td>
                    <td style={styles.tableCell}>{item.productName || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.productCategory || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.unit || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.origin || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.sellingPrice || 0}</td>
                    <td style={styles.tableCell}>{item.currentStock || 0}</td>
                    <td style={styles.tableCell}>{item.importPrice || 0}</td>
                    <td style={styles.tableCell}>{item.discount || 0}</td>
                    <td style={styles.tableCell}>{item.importAmount || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p></p>
          )}
        </div>
      ) : (
        <p></p>
      )}

{productAdjust ? (
        <div style={styles.infoContent}>
          {/* Product Information */}
          <h1>Thông tin sản phẩm</h1>
          {productAdjust.importDetail && productAdjust.importDetail.length > 0 ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
              <thead>
                <tr>
                  <th style={styles.tableHeader}>STT</th>
                  <th style={styles.tableHeader}>Tên sản phẩm</th>
                  <th style={styles.tableHeader}>Danh mục sản phẩm</th>
                  <th style={styles.tableHeader}>Đơn vị</th>
                  <th style={styles.tableHeader}>Xuất xứ</th>
                  <th style={styles.tableHeader}>Giá bán</th>
                  <th style={styles.tableHeader}>Kho hiện tại</th>
                  <th style={styles.tableHeader}>Giá nhập</th>
                  <th style={styles.tableHeader}>Phần trăm giảm giá</th>
                </tr>
              </thead>
              <tbody>
                {productAdjust.importDetail.map((item, index) => (
                  <tr key={index}>
                    <td style={styles.tableCell}>{index + 1}</td>
                    <td style={styles.tableCell}>{item.productName || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.productCategory || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.unit || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.origin || 'N/A'}</td>
                    <td style={styles.tableCell}>{item.sellingPrice || 0}</td>
                    <td style={styles.tableCell}>{item.currentStock || 0}</td>
                    <td style={styles.tableCell}>{item.importPrice || 0}</td>
                    <td style={styles.tableCell}>{item.discount || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p></p>
          )}
        </div>
      ) : (
        <p></p>
      )}

      {businessInfo?.monthlyDetails?.length > 0 ? (
        <div style={styles.infoContent}>
          {/* Business Analysis Information */}
          <h1>Thông tin phân tích kinh doanh</h1>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
            <thead>
              <tr>
                <th style={styles.tableHeader}>Tháng</th>
                <th style={styles.tableHeader}>Doanh thu tổng (VNĐ)</th>
                <th style={styles.tableHeader}>Lợi nhuận gộp (VNĐ)</th>
                <th style={styles.tableHeader}>Số tiền chưa ghi nhận (VNĐ)</th>
              </tr>
            </thead>
            <tbody>
              {businessInfo.monthlyDetails.map((monthDetail, index) => (
                <tr key={index}>
                  <td style={styles.tableCell}>{monthDetail.month || 'N/A'}</td>
                  <td style={styles.tableCell}>{monthDetail.totalRevenue?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{monthDetail.grossProfit?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{monthDetail.unreceivedAmount?.toLocaleString("vi-VN") || 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p></p>
      )}

      {debtInfo?.length > 0 ? (
        <div style={styles.infoContent}>
          {/* Customer Debt Information */}
          <h1>Thông tin nợ khách hàng</h1>
          {debtInfo.map((customer, customerIndex) => (
            <div key={customerIndex} style={{ marginBottom: '20px' }}>
              <h4>Khách hàng: {customer.name} (ID: {customer.identifier})</h4>
              <p>Số điện thoại: {customer.phone_number}</p>
              <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
                <thead>
                  <tr>
                    <th style={styles.tableHeader}>Ngày đến hạn</th>
                    <th style={styles.tableHeader}>Tổng số nợ (VNĐ)</th>
                  </tr>
                </thead>
                <tbody>
                  {customer.related_data.map((debtDetail, index) => (
                    <tr key={index}>
                      <td style={styles.tableCell}>{debtDetail.due_date || 'N/A'}</td>
                      <td style={styles.tableCell}>{debtDetail.total_debt?.toLocaleString("vi-VN") || '0'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
        </div>
      ) : (
        <p></p>
      )}

      {inactiveInfo?.length > 0 ? (
        <div style={styles.infoContent}>
          {/* Business Analysis Information */}
          <h1>Thông tin khách hàng ít mua hàng</h1>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
            <thead>
              <tr>
                <th style={styles.tableHeader}>STT</th>
                <th style={styles.tableHeader}>Mã khách hàng</th>
                <th style={styles.tableHeader}>Tên khách hàng</th>
                <th style={styles.tableHeader}>Lần cuối mua hàng</th>
              </tr>
            </thead>
            <tbody>
              {inactiveInfo.map((inactiveDetail, index) => (
                <tr key={index}>
                  <td style={styles.tableCell}>{index + 1}</td>
                  <td style={styles.tableCell}>{inactiveDetail.identifier?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{inactiveDetail.name?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{inactiveDetail.date_field?.toLocaleString("vi-VN") || 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p></p>
      )}

      {restockAlertInfo?.length > 0 ? (
        <div style={styles.infoContent}>
          {/* Business Analysis Information */}
          <h1>Thông tin phân tích kho</h1>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
            <thead>
              <tr>
                <th style={styles.tableHeader}>STT</th>
                <th style={styles.tableHeader}>Mã sản phẩm </th>
                <th style={styles.tableHeader}>Tên sản phẩm</th>
                <th style={styles.tableHeader}>Số lượng tồn kho</th>
                <th style={styles.tableHeader}>Ngưỡng nhập hàng</th>
              </tr>
            </thead>
            <tbody>
              {restockAlertInfo.map((restockAlertDetail, index) => (
                <tr key={index}>
                  <td style={styles.tableCell}>{index + 1}</td>
                  <td style={styles.tableCell}>{restockAlertDetail.identifier?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{restockAlertDetail.productName?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{restockAlertDetail.currentStock?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{restockAlertDetail.reOrderLevel?.toLocaleString("vi-VN") || 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p></p>
      )}  

    {trendInfo?.length > 0 ? (
        <div style={styles.infoContent}>
          {/* Business Analysis Information */}
          <h1>Thông tin phân tích mặt hàng bán chạy</h1>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '10px' }}>
            <thead>
              <tr>
                <th style={styles.tableHeader}>STT</th>
                <th style={styles.tableHeader}>Tên sản phẩm </th>
                <th style={styles.tableHeader}>Tổng số lượng bán</th>
                <th style={styles.tableHeader}>Tổng doanh thu</th>
                <th style={styles.tableHeader}>Tổng số lần bán</th>
              </tr>
            </thead>
            <tbody>
              {trendInfo.map((trendInfoDetail, index) => (
                <tr key={index}>
                  <td style={styles.tableCell}>{index + 1}</td>
                  <td style={styles.tableCell}>{trendInfoDetail.productName?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{trendInfoDetail.totalQuantitySold?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{trendInfoDetail.totalRevenue?.toLocaleString("vi-VN") || 0}</td>
                  <td style={styles.tableCell}>{trendInfoDetail.saleFrequency?.toLocaleString("vi-VN") || 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p></p>
      )}  


      </div>
    </div>
  );
};

// Styles for the chat box
const styles = {
  container: {
    display: 'flex',
    flexDirection: 'row',
    width: '100%',
    height: 'calc(80vh - 40px)',
    padding: '10px',
    boxSizing: 'border-box',
    overflow: 'hidden',
  },
  chatContainer: {
    width: '100%',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    border: '1px solid #ccc',
    borderRadius: '10px',
    overflow: 'hidden',
    backgroundColor: '#2986cc',
  },
  
  infoContent: {
    marginTop: '10px',
  },
  chatMessages: {
    padding: '10px',
    flex: 1,
    overflowY: 'auto',
    backgroundColor: '#ffffff',
    wordBreak: 'break-word', // Ensure messages wrap properly within the box
    overflowWrap: 'break-word',
  },
  inputContainer: {
    padding: '10px',
    borderTop: '1px solid #ccc',
    backgroundColor: '#fff',
  },
  textarea: {
    width: '100%',
    height: '120px',
    padding: '10px',
    borderRadius: '5px',
    border: '1px solid #ccc',
    resize: 'none',
    fontSize: '16px',
    boxSizing: 'border-box',
    wordWrap: 'break-word',
  },
  tableHeader: {
    border: '1px solid black',
    padding: '5px',
    backgroundColor: '#f2f2f2',
  },
  tableCell: {
    border: '1px solid black',
    padding: '5px',
  },
};

export default ChatBoxMydio;

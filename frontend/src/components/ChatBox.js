import React, { useRef, useState, useCallback, useEffect } from 'react';
import SpeechRecognition, { useSpeechRecognition } from 'react-speech-recognition';
import { Button as ChatButton } from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { materialDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { FaMicrophone } from 'react-icons/fa';

const ChatBox = ({ model, modelType, sessionId, connected, chatLog, setChatLog }) => {
  console.log('ChatBox component rendered with props:', { model, modelType, sessionId, connected, chatLog });
  const systemRole = ``;

  const [inputValue, setInputValue] = useState('');
  const [ambiguousInfo, setAmbiguousInfo] = useState(null);
  const [billInfo, setBillInfo] = useState(null);
  const [productInfo, setProductInfo] = useState(null);
  const [productAdjust, setProductAdjust] = useState(null);
  const [businessInfo, setBusinessInfo] = useState(null);
  const [inactiveInfo, setInactiveInfo] = useState(null);
  const [debtInfo, setDebtInfo] = useState(null);
  const [restockAlertInfo, setRestockAlertInfo] = useState(null);
  const [trendInfo, setTrendInfo] = useState(null);
  const chatRef = useRef(null);
  const typingTimeoutRef = useRef(null); // Ref for managing the typing timeout
  const [isSubmitting, setIsSubmitting] = useState(false);


  const {
    transcript,
    listening,
    resetTranscript,
    browserSupportsSpeechRecognition
  } = useSpeechRecognition();

  if (!browserSupportsSpeechRecognition) {
    return <span>Browser doesn't support speech recognition.</span>;
  }

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

  const handleChatSubmit = useCallback(
    async (message) => {
      console.log('handleChatSubmit called with message:', message);
      if (!connected || !model) {
        alert('Please connect to a model and database before chatting.');
        return;
      }
  
      // Prevent multiple submissions
      if (isSubmitting) {
        console.log('Already submitting, skipping duplicate submission.');
        return;
      }
  
      setIsSubmitting(true);
  
      // Update the chat log using setChatLog passed from the parent component
      const newChatLog = [...chatLog, { user: 'user', text: message }];
      console.log('Updating chat log:', newChatLog);
      setChatLog(newChatLog);
      setInputValue('');
      resetTranscript(); // <-- Added here to reset the transcript after sending the message
  
      try {
        SpeechRecognition.stopListening();
        if (modelType === 'api') {
          await callApiEndpoint(message, model);
        } else if (modelType === 'mlflow') {
          await callMlflowEndpoint(message, model);
        }
      } catch (error) {
        console.error('Error fetching response:', error);
        setChatLog((prevChatLog) => [
          ...prevChatLog,
          { user: 'bot', text: 'Error fetching response' },
        ]);
      } finally {
        setIsSubmitting(false);
      }
    },
    [chatLog, model, modelType, sessionId, connected, setChatLog, isSubmitting, resetTranscript] // Ensure all dependencies are included
  );
  
  


  

  const callApiEndpoint = async (message, model) => {
    console.log('callApiEndpoint called with message and model:', message, model);
    try {
      const response = await fetch('https://localhost:8888/v2/vPOS_chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, model, sessionId, systemRole }),
      });
  
      console.log('API response status:', response.status);
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      const result = await response.json();
      console.log('API response result:', result);
  
      const messages = [];
  
      if (result.fullResponse?.trim()) {
        messages.push({ text: result.fullResponse, user: 'bot' });
      }
  
      if (result.sqlQuery?.trim()) {
        messages.push({
          text: `**SQL Query:**\n\`\`\`sql\n${result.sqlQuery}\n\`\`\``,
          user: 'bot',
        });
      }
  
      if (Array.isArray(result.queryResult) && result.queryResult.length > 0) {
        const table = generateTable(result.queryResult);
        messages.push({ text: `**Result:**\n${table}`, user: 'bot' });
      }
  
      // Add messages with delay
      console.log('Adding messages with delay:', messages);
      addMessagesWithDelay(messages, 1000);
  
      // Check if audio data is available
      if (result.audio) {
        console.log('Audio data found, playing audio.');
        // Stop listening before playing audio
        SpeechRecognition.stopListening();
        // Decode Base64 audio and play it
        const audioBase64 = result.audio;
        const audioBlob = new Blob([new Uint8Array(atob(audioBase64).split('').map(c => c.charCodeAt(0)))], { type: 'audio/wav' });
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = new Audio(audioUrl);
        audio.play();
        // Automatically play the audio
        audio.addEventListener('ended', () => {
          URL.revokeObjectURL(audioUrl);
          // Restart listening after the audio ends
          SpeechRecognition.startListening({ continuous: true });
        });
      }

      // Update bill information if provided
      if (result.billInfo) {
        console.log('Updating bill info:', result.billInfo);
        // Clear other states
        setRestockAlertInfo(null);
        setProductInfo(null);
        setProductAdjust(null);
        setBusinessInfo(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
        setBillInfo(result.billInfo);
      }
      
      if (result.productInfo) {
        console.log('Updating product info:', result.productInfo);
        // Clear other states
        setBillInfo(null);
        setRestockAlertInfo(null);
        setProductAdjust(null);
        setBusinessInfo(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
        setProductInfo(result.productInfo);
      }
      
      if (result.productAdjust) {
        console.log('Updating product adjust info:', result.productAdjust);
        // Clear other states
        setBillInfo(null);
        setProductInfo(null);
        setRestockAlertInfo(null);
        setBusinessInfo(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
        setProductAdjust(result.productAdjust);
      }
      
      if (result.businessInfo) {
        console.log('Updating business info:', result.businessInfo);
        // Clear other states
        setBillInfo(null);
        setRestockAlertInfo(null);
        setProductInfo(null);
        setProductAdjust(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
        setBusinessInfo(result.businessInfo);
      }
      
      if (result.debtInfo) {
        console.log('Updating bill info:', result.billInfo);
        // Clear other states
        setProductInfo(null);
        setProductAdjust(null);
        setRestockAlertInfo(null);
        setBusinessInfo(null);
        setBillInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
        setDebtInfo(result.debtInfo);
      }

      if (result.inactiveInfo) {
        console.log('Updating bill info:', result.billInfo);
        // Clear other states
        setProductInfo(null);
        setProductAdjust(null);
        setRestockAlertInfo(null);
        setBusinessInfo(null);
        setBillInfo(null);
        setDebtInfo(null);
        setTrendInfo(null);
        setInactiveInfo(result.inactiveInfo);
      }

      if (result.restockAlertInfo) {
        console.log('Updating bill info:', result.billInfo);
        // Clear other states
        setProductInfo(null);
        setProductAdjust(null);
        setRestockAlertInfo(result.restockAlertInfo);
        setBusinessInfo(null);
        setBillInfo(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(null);
      }

      if (result.trendInfo) {
        setProductInfo(null);
        setProductAdjust(null);
        setRestockAlertInfo(null);
        setBusinessInfo(null);
        setBillInfo(null);
        setDebtInfo(null);
        setInactiveInfo(null);
        setTrendInfo(result.trendInfo);
      }
  
    } catch (error) {
      console.error('Error with API models:', error);
      setChatLog((prevChatLog) => [
        ...prevChatLog,
        { user: 'bot', text: 'Error with API model response' },
      ]);
    }
  };

  const addMessagesWithDelay = (messages, delay) => {
    console.log('addMessagesWithDelay called with messages and delay:', messages, delay);
    messages.forEach((message, index) => {
      setTimeout(() => {
        console.log('Adding message to chat log:', message);
        setChatLog((prevChatLog) => [...prevChatLog, message]);
      }, index * delay);
    });
  };

  const handleMicrophoneClick = () => {
    console.log('handleMicrophoneClick called, listening:', listening);
    if (listening) {
      SpeechRecognition.stopListening();
      console.log('Stopped listening');
    } else {
      SpeechRecognition.startListening({ continuous: true });
      console.log('Started listening');
    }
  };

  // useEffect to handle new transcript
  useEffect(() => {
    if (listening && transcript) {
      console.log('Transcript updated:', transcript);
      setInputValue(transcript);
  
      // Clear the previous timer
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
  
      // Set a new timer to automatically send the message after 2 seconds of silence
      typingTimeoutRef.current = setTimeout(() => {
        console.log('2 seconds of silence, auto-sending message:', transcript);
        if (transcript.trim()) {
          handleChatSubmit(transcript);
          SpeechRecognition.stopListening(); // Reset the transcript after sending the message
        }
      }, 1000); // Set to 2000 milliseconds (2 seconds)
    }
  }, [listening, transcript, handleChatSubmit]);
  
  

  // Scroll to the bottom whenever chatLog updates
  useEffect(() => {
    console.log('useEffect triggered for chatLog update:', chatLog);
    chatRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatLog]);

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

        <div style={styles.inputContainer}>
        <textarea
          placeholder="Type message here"
          value={inputValue}
          onChange={(e) => {
            setInputValue(e.target.value);
            SpeechRecognition.stopListening(); // Stop listening when the user starts typing manually
          }}
          style={styles.textarea}
        />


          <div style={{ display: 'flex', alignItems: 'center', marginTop: '10px' }}>
            <ChatButton
              text="Send"
              onClick={() => {
                console.log('Send button clicked, inputValue:', inputValue);
                if (inputValue.trim()) {
                  handleChatSubmit(inputValue);
                }
              }}
            />
            <FaMicrophone
              onClick={handleMicrophoneClick}
              style={{
                marginLeft: '10px',
                cursor: 'pointer',
                color: listening ? 'red' : 'black',
              }}
              size={24}
            />
          </div>
        </div>
        
      </div>
      <div style={styles.infoContainer}>
        <div style={styles.newInfoContainer}>

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
      <div style={styles.ambiguousInfoContainer}>
      <h3>Thông tin không rõ ràng</h3>
          {ambiguousInfo ? (
            <div style={styles.infoContent}>
              <h3>Thông tin không rõ ràng</h3>
              <p>{ambiguousInfo}</p>
            </div>
          ) : (
            <p></p>
          )}
        </div>
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
    width: '50%',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    border: '1px solid #ccc',
    borderRadius: '10px',
    overflow: 'hidden',
    backgroundColor: '#2986cc',
  },
  infoContainer: {
    width: '50%',
    padding: '10px',
    border: '1px solid #ccc',
    borderRadius: '10px',
    marginLeft: '10px',
    backgroundColor: '#fff',
    overflow: 'auto',
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
  },
  newInfoContainer: {
    flex: 7,
    padding: '10px',
    borderBottom: '1px solid #ccc',
  },
  ambiguousInfoContainer: {
    flex: 3,
    padding: '10px',
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

export default ChatBox;

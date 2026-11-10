// Read only local test inboxes; never expose OTPs through the application API.
export async function containerCode(id){
 const sms=await(await fetch('http://127.0.0.1:8026/messages')).json();
 const message=sms.messages.find(m=>m.id===id);
 if(message)return message.text.match(/code is (\d{6})/)[1];
 const email=await(await fetch('http://127.0.0.1:18025/api/v1/search?query='+encodeURIComponent('subject:'+id))).json();
 const match=email.messages?.find(m=>m.Subject.includes(id));
 if(!match)throw Error('Code not yet received');
 const full=await(await fetch('http://127.0.0.1:18025/api/v1/message/'+match.ID)).json();
 return full.Text.match(/code is (\d{6})/)[1];
}

import React, { useState } from 'react';
import collegeLogo from '../assets/college-logo.png';
import { Mail, Lock, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { isValidEmail } from '../lib/validation';
import { supabase } from '../lib/supabase';

export function LoginPage({ onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [emailTouched, setEmailTouched] = useState(false);
  const [formSubmitted, setFormSubmitted] = useState(false);
  const [generalError, setGeneralError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const isEmailValid = isValidEmail(email);
  const showEmailError = (emailTouched || formSubmitted || email.length > 0) && !isEmailValid;

  const handleEmailChange = (e) => {
    setEmail(e.target.value);
    setEmailTouched(true);
    setGeneralError('');
  };

  const handleEmailBlur = () => {
    setEmailTouched(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormSubmitted(true);
    setEmailTouched(true);

    // Prevent submission if email is invalid
    if (!isValidEmail(email)) {
      return;
    }

    if (!password.trim()) {
      setGeneralError('Please enter your administrator password.');
      return;
    }

    setIsLoading(true);
    setGeneralError('');

    const cleanEmail = email.trim().toLowerCase();

    // Pure, strict Supabase authentication — No hardcoded bypass or backdoor
    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email: cleanEmail,
        password: password
      });

      if (error) {
        setIsLoading(false);
        setGeneralError(error.message || 'Invalid email or password. Please verify your credentials.');
        return;
      }

      const userRole = data?.user?.user_metadata?.role?.toLowerCase() || '';

      if (userRole !== 'admin') {
        await supabase.auth.signOut();
        setIsLoading(false);
        setGeneralError('Access denied: Administrator privileges required for this portal.');
        return;
      }

      setIsLoading(false);
      if (onLoginSuccess) {
        onLoginSuccess({ role: 'admin', email: cleanEmail, user: data.user, session: data.session });
      }
    } catch (err) {
      setIsLoading(false);
      setGeneralError('Authentication failed. Please verify your connection.');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center items-center px-4 py-8 sm:px-6 lg:px-8">
      {/* College Logo / Header */}
      <div className="text-center max-w-md w-full mb-6">
        <img 
          src={collegeLogo} 
          alt="L.D.A.H Rajkiya Engineering College Mainpuri Logo" 
          className="w-20 h-20 mx-auto object-contain drop-shadow-md mb-3" 
          onError={(e) => { e.currentTarget.src = '/college-logo.png'; }}
        />
        <h2 className="text-base font-extrabold text-slate-900 tracking-tight leading-snug">
          Lokmata Devi Ahilya Bai Holkar Rajkiya Engineering College, Mainpuri
        </h2>
        <p className="text-xs font-semibold text-brand-600 uppercase tracking-wider mt-1">
          Smart Campus Portal
        </p>
      </div>

      {/* Main Login Card */}
      <div className="bg-white border border-slate-200/80 rounded-2xl shadow-sm max-w-md w-full p-6 sm:p-8">
        <div className="flex items-center gap-2 mb-1">
          <ShieldCheck className="w-5 h-5 text-[#1A2A49]" />
          <h3 className="text-xl font-bold text-slate-900">Administrator Sign In</h3>
        </div>
        <p className="text-xs text-slate-500 mb-6">
          Enter administrator credentials to access the control portal
        </p>

        <form onSubmit={handleSubmit} noValidate className="space-y-4">
          {/* Email Input Field */}
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1.5">
              Administrator Email ID
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                value={email}
                onChange={handleEmailChange}
                onBlur={handleEmailBlur}
                placeholder="Enter email address"
                className={`w-full pl-9 pr-3 py-2.5 text-xs bg-white border rounded-xl text-slate-900 placeholder:text-slate-400 focus:outline-none transition-colors ${
                  showEmailError
                    ? 'border-[#D8524F] ring-1 ring-[#D8524F] focus:border-[#D8524F]'
                    : 'border-slate-300 focus:border-[#1A2A49] focus:ring-1 focus:ring-[#1A2A49]'
                }`}
              />
            </div>
            {showEmailError && (
              <p className="text-[11px] text-[#D8524F] mt-1 pl-1">
                Please enter a valid email address.
              </p>
            )}
          </div>

          {/* Password Input Field */}
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1.5">
              Password
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setGeneralError('');
                }}
                placeholder="Enter password"
                className="w-full pl-9 pr-10 py-2.5 text-xs bg-white border border-slate-300 rounded-xl text-slate-900 placeholder:text-slate-400 focus:outline-none focus:border-[#1A2A49] focus:ring-1 focus:ring-[#1A2A49] transition-colors"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* General error message if any */}
          {generalError && (
            <p className="text-xs text-rose-500 font-medium pl-1">{generalError}</p>
          )}

          {/* Sign In Button */}
          <button
            type="submit"
            disabled={isLoading}
            className="w-full mt-2 py-3 px-4 rounded-xl text-xs font-semibold text-white bg-[#1A2A49] hover:bg-[#132038] disabled:bg-slate-300 disabled:cursor-not-allowed shadow-sm transition-all"
          >
            {isLoading ? 'Signing in...' : 'Sign In as Administrator'}
          </button>
        </form>
      </div>

      {/* IT Helpdesk Footer */}
      <div className="text-center mt-6 text-xs text-slate-500">
        <p>Need help accessing your account? Contact IT Desk</p>
        <p className="font-semibold text-slate-700 mt-1">
          helpdesk@recmainpuri.ac.in | Ext: 404
        </p>
      </div>
    </div>
  );
}

export default LoginPage;

import { createClient } from '@supabase/supabase-js';

export const SUPABASE_PROJECT_ID = 'easrebumarwkloibtemf';
export const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL || `https://${SUPABASE_PROJECT_ID}.supabase.co`;
export const SUPABASE_PUBLISHABLE_KEY = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY || 'sb_publishable_CsQmrZ6ejrBeu6sQlmQX7Q_UoEoomC6';

export const supabase = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, {
  auth: {
    persistSession: true,
    autoRefreshToken: true
  }
});

// Secondary client that does NOT persist sessions in localStorage so Admin's active session is never overwritten
export const authClient = createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY, {
  auth: {
    persistSession: false,
    autoRefreshToken: false,
    detectSessionInUrl: false
  }
});

/**
 * Diagnostic utility to test Supabase connection and check table availability
 */
export async function checkSupabaseStatus() {
  try {
    const startTime = performance.now();
    const { data, error } = await supabase.from('students').select('id').limit(1);
    const latency = Math.round(performance.now() - startTime);

    if (!error) {
      return {
        connected: true,
        tablesExist: true,
        latency,
        projectId: SUPABASE_PROJECT_ID,
        url: SUPABASE_URL,
        message: 'Connected to Supabase PostgreSQL database successfully.'
      };
    }

    if (error.code === 'PGRST205' || error.message?.includes('Could not find the table')) {
      return {
        connected: true,
        tablesExist: false,
        latency,
        projectId: SUPABASE_PROJECT_ID,
        url: SUPABASE_URL,
        message: 'Supabase API is connected, but PostgreSQL tables are not yet created in the project. Please run the schema SQL.'
      };
    }

    return {
      connected: false,
      tablesExist: false,
      latency,
      projectId: SUPABASE_PROJECT_ID,
      url: SUPABASE_URL,
      message: error.message || 'Supabase query error'
    };
  } catch (err) {
    return {
      connected: false,
      tablesExist: false,
      latency: 0,
      projectId: SUPABASE_PROJECT_ID,
      url: SUPABASE_URL,
      message: err.message || 'Connection failed'
    };
  }
}
